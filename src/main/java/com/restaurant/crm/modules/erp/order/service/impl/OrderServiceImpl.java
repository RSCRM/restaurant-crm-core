package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderBlockedItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderItemStatus> ORDER_CANCELLABLE_ORDER_ITEM_STATUSES =
            EnumSet.of(OrderItemStatus.PENDING, OrderItemStatus.CANCELLED);

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    OrganizationBranchRepository organizationBranchRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    RestaurantTableRepository restaurantTableRepository;
    CustomerSseService customerSseService;

    @Override
    @Transactional
    public CreateOrderResponse create(CreateOrderRequestDto request) {
        String branchId = request.getBranchId();

        if (!StringUtils.hasText(branchId)) {
            throw new AppException(ErrorCode.ORDER_BRANCH_ID_REQUIRED);
        }

        if (!organizationBranchRepository.existsById(branchId)) {
            throw new AppException(ErrorCode.ORDER_BRANCH_NOT_FOUND);
        }

        RestaurantTable table = null;
        if (request.getOrderType() == OrderType.DINE_IN) {
            if (!StringUtils.hasText(request.getTableId())) {
                throw new AppException(ErrorCode.ORDER_TABLE_ID_REQUIRED);
            }

            table = restaurantTableRepository.findById(request.getTableId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND));

            if (!restaurantTableRepository.existsByIdAndAreaBranchId(request.getTableId(), branchId)) {
                throw new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND);
            }
        }

        if (table != null && table.getStatus() == RestaurantTableStatus.OCCUPIED) {
            Order activeOrder = orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc(table.getId(), OrderStatus.PENDING)
                    .orElse(null);
            if (activeOrder != null) {
                BigDecimal orderSubtotal = activeOrder.getSubtotal();
                for (CreateOrderItemRequestDto itemRequest : request.getItems()) {
                    BigDecimal unitPrice = resolveUnitPrice(activeOrder.getBranchId(), itemRequest.getProductId(), itemRequest.getComboId());

                    OrderItem orderItem = OrderItem.builder()
                            .order(activeOrder)
                            .productId(itemRequest.getProductId())
                            .comboId(itemRequest.getComboId())
                            .quantity(itemRequest.getQuantity())
                            .unitPrice(unitPrice)
                            .subtotal(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                            .note(itemRequest.getNote())
                            .status(OrderItemStatus.PENDING)
                            .build();
                    OrderItem savedOrderItem = orderItemRepository.save(orderItem);

                    BigDecimal modifierTotal = saveOrderItemModifiers(savedOrderItem, itemRequest.getModifiers());
                    BigDecimal itemSubtotal = savedOrderItem.getSubtotal().add(modifierTotal);
                    savedOrderItem.setSubtotal(itemSubtotal);
                    orderItemRepository.save(savedOrderItem);

                    orderSubtotal = orderSubtotal.add(itemSubtotal);
                }

                activeOrder.setSubtotal(orderSubtotal);
                activeOrder.setTotalAmount(orderSubtotal.subtract(activeOrder.getDiscountAmount()));
                orderRepository.save(activeOrder);

                OrderCookingStatusResponse updatedStatus = getOrderCookingStatus(activeOrder.getId());
                customerSseService.broadcastOrderUpdate(activeOrder.getId(), updatedStatus);

                return CreateOrderResponse.builder()
                        .orderId(activeOrder.getId())
                        .build();
            }
        }

        Order newOrder = Order.builder()
                .branchId(branchId)
                .tableId(request.getTableId())
                .reservationId(request.getReservationId())
                .orderCode(generateOrderCode())
                .orderType(request.getOrderType())
                .status(OrderStatus.PENDING)
                .customerName(request.getCustomerName())
                .customerPhone(request.getCustomerPhone())
                .note(request.getNote())
                .subtotal(BigDecimal.ZERO)
                .discountAmount(BigDecimal.ZERO)
                .totalAmount(BigDecimal.ZERO)
                .build();

        Order savedOrder = orderRepository.save(newOrder);

        BigDecimal orderSubtotal = BigDecimal.ZERO;
        for (CreateOrderItemRequestDto itemRequest : request.getItems()) {
            BigDecimal unitPrice = resolveUnitPrice(savedOrder.getBranchId(), itemRequest.getProductId(), itemRequest.getComboId());

            OrderItem orderItem = OrderItem.builder()
                    .order(savedOrder)
                    .productId(itemRequest.getProductId())
                    .comboId(itemRequest.getComboId())
                    .quantity(itemRequest.getQuantity())
                    .unitPrice(unitPrice)
                    .subtotal(unitPrice.multiply(BigDecimal.valueOf(itemRequest.getQuantity())))
                    .status(OrderItemStatus.PENDING)
                    .note(itemRequest.getNote())
                    .build();
            OrderItem savedOrderItem = orderItemRepository.save(orderItem);

            BigDecimal modifierTotal = saveOrderItemModifiers(savedOrderItem, itemRequest.getModifiers());
            BigDecimal itemSubtotal = savedOrderItem.getSubtotal().add(modifierTotal);
            savedOrderItem.setSubtotal(itemSubtotal);
            orderItemRepository.save(savedOrderItem);

            orderSubtotal = orderSubtotal.add(itemSubtotal);
        }

        savedOrder.setSubtotal(orderSubtotal);
        savedOrder.setTotalAmount(orderSubtotal.subtract(savedOrder.getDiscountAmount()));
        orderRepository.save(savedOrder);

        if (table != null) {
            table.setStatus(RestaurantTableStatus.OCCUPIED);
            restaurantTableRepository.save(table);
        }

        return CreateOrderResponse.builder()
                .orderId(savedOrder.getId())
                .build();
    }

    @Override
    @Transactional
    public CancelOrderResponse cancelOrder(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        validateOrderStatusForOrderCancellation(order);

        List<OrderItem> orderItems = orderItemRepository.findByOrderId(orderId);
        List<CancelOrderBlockedItemResponse> blockedItems = getBlockedItemsForOrderCancellation(orderItems);
        if (!blockedItems.isEmpty()) {
            return CancelOrderResponse.builder()
                    .cancelled(false)
                    .blockedItems(blockedItems)
                    .build();
        }

        List<OrderItem> itemsToCancel = orderItems.stream()
                .filter(orderItem -> orderItem.getStatus() != OrderItemStatus.CANCELLED)
                .toList();
        if (!itemsToCancel.isEmpty()) {
            for (OrderItem orderItem : itemsToCancel) {
                orderItem.setStatus(OrderItemStatus.CANCELLED);
            }
            orderItemRepository.saveAll(itemsToCancel);
        }

        order.setStatus(OrderStatus.CANCELLED);
        order.setSubtotal(BigDecimal.ZERO);
        order.setTotalAmount(BigDecimal.ZERO);
        orderRepository.save(order);

        return CancelOrderResponse.builder()
                .cancelled(true)
                .build();
    }

    @Override
    public OrderCookingStatusResponse getOrderCookingStatus(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        List<OrderItem> items = orderItemRepository.findByOrderId(orderId);
        List<OrderItemCookingStatusResponse> itemResponses = new ArrayList<>();

        for (OrderItem item : items) {
            String itemName = "Unknown Dish";
            if (item.getProductId() != null) {
                Product product = productRepository.findById(item.getProductId()).orElse(null);
                if (product != null) {
                    itemName = product.getProductName();
                }
            } else if (item.getComboId() != null) {
                Combo combo = comboRepository.findById(item.getComboId()).orElse(null);
                if (combo != null) {
                    itemName = combo.getComboName();
                }
            }

            itemResponses.add(OrderItemCookingStatusResponse.builder()
                    .orderItemId(item.getId())
                    .itemName(itemName)
                    .quantity(item.getQuantity())
                    .note(item.getNote())
                    .status(item.getStatus())
                    .updatedAt(item.getUpdatedAt())
                    .build());
        }

        return OrderCookingStatusResponse.builder()
                .orderId(order.getId())
                .orderCode(order.getOrderCode())
                .tableId(order.getTableId())
                .customerPhone(order.getCustomerPhone())
                .status(order.getStatus())
                .items(itemResponses)
                .updatedAt(order.getUpdatedAt())
                .build();
    }

    @Override
    public OrderCookingStatusResponse getActiveOrderCookingStatusByTable(String tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND));

        if (table.getStatus() != RestaurantTableStatus.OCCUPIED) {
            throw new AppException(ErrorCode.ORDER_NOT_FOUND);
        }

        Order activeOrder = orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc(tableId, OrderStatus.PENDING)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        return getOrderCookingStatus(activeOrder.getId());
    }

    private String generateOrderCode() {
        return OrderConstants.ORDER_CODE_PREFIX
                + UUID.randomUUID().toString().substring(0, OrderConstants.ORDER_CODE_RANDOM_LENGTH).toUpperCase();
    }

    private void validateOrderStatusForOrderCancellation(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
    }

    private BigDecimal resolveUnitPrice(String branchId, String productId, String comboId) {
        if (StringUtils.hasText(productId)) {
            Product product = productRepository.findByIdAndBranchId(productId, branchId)
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_PRODUCT_NOT_FOUND));
            return product.getPrice();
        }

        Combo combo = comboRepository.findByIdAndBranchId(comboId, branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_COMBO_NOT_FOUND));
        return combo.getPrice();
    }

    private BigDecimal saveOrderItemModifiers(
            OrderItem orderItem,
            List<CreateOrderItemModifierRequestDto> modifierRequests
    ) {
        if (modifierRequests == null || modifierRequests.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal modifierTotal = BigDecimal.ZERO;
        List<OrderItemModifier> modifiersToSave = new ArrayList<>();
        for (CreateOrderItemModifierRequestDto modifierRequest : modifierRequests) {
            ModifierOption modifierOption = modifierOptionRepository.findById(modifierRequest.getModifierOptionId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORDER_MODIFIER_OPTION_NOT_FOUND));
            BigDecimal additionalPrice = modifierOption.getAdditionalPrice();
            modifierTotal = modifierTotal.add(
                    additionalPrice.multiply(BigDecimal.valueOf(modifierRequest.getQuantity()))
            );

            modifiersToSave.add(OrderItemModifier.builder()
                    .orderItem(orderItem)
                    .modifierOptionId(modifierRequest.getModifierOptionId())
                    .additionalPrice(additionalPrice)
                    .quantity(modifierRequest.getQuantity())
                    .build());
        }

        orderItemModifierRepository.saveAll(modifiersToSave);
        return modifierTotal;
    }

    private List<CancelOrderBlockedItemResponse> getBlockedItemsForOrderCancellation(List<OrderItem> orderItems) {
        List<CancelOrderBlockedItemResponse> blockedItems = new ArrayList<>();
        for (OrderItem orderItem : orderItems) {
            if (ORDER_CANCELLABLE_ORDER_ITEM_STATUSES.contains(orderItem.getStatus())) {
                continue;
            }

            blockedItems.add(CancelOrderBlockedItemResponse.builder()
                    .orderItemId(orderItem.getId())
                    .productId(orderItem.getProductId())
                    .comboId(orderItem.getComboId())
                    .quantity(orderItem.getQuantity())
                    .status(orderItem.getStatus())
                    .note(orderItem.getNote())
                    .build());
        }

        return blockedItems;
    }
}
