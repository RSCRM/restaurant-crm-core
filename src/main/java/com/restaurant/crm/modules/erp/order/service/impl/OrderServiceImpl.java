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
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
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
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderStatus> MODIFIABLE_ORDER_STATUSES =
            EnumSet.of(OrderStatus.PENDING);
    private static final Set<OrderItemStatus> MODIFIABLE_ORDER_ITEM_STATUSES =
            EnumSet.of(OrderItemStatus.PENDING);
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
    com.restaurant.crm.modules.crm.loyalty_voucher.service.interfaces.CustomerVoucherService customerVoucherService;
    com.restaurant.crm.modules.crm.loyalty_voucher.repository.CustomerVoucherRepository customerVoucherRepository;
    com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository customerRepository;
    com.restaurant.crm.modules.crm.point_wallet.service.interfaces.PointWalletService pointWalletService;
    SseEmitterService sseEmitterService;


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
                com.restaurant.crm.modules.crm.loyalty_voucher.entity.CustomerVoucher appliedVoucher = customerVoucherRepository.findByOrderId(activeOrder.getId()).orElse(null);
                if (appliedVoucher != null) {
                    BigDecimal discountPercent = BigDecimal.valueOf(appliedVoucher.getVoucher().getDiscountPercent());
                    BigDecimal discountAmount = orderSubtotal.multiply(discountPercent).divide(BigDecimal.valueOf(100));
                    activeOrder.setDiscountAmount(discountAmount);
                    activeOrder.setTotalAmount(orderSubtotal.subtract(discountAmount));
                } else {
                    activeOrder.setTotalAmount(orderSubtotal.subtract(activeOrder.getDiscountAmount()));
                }
                orderRepository.save(activeOrder);

                OrderCookingStatusResponse updatedStatus = getOrderCookingStatus(activeOrder.getId());
                customerSseService.broadcastOrderUpdate(activeOrder.getId(), updatedStatus);

                // Broadcast KDS update to kitchen
                sseEmitterService.broadcastToBranch(
                        activeOrder.getBranchId(),
                        "KDS_ORDER_UPDATED",
                        activeOrder.getId(),
                        StartDefinedOrgPermission.ORDER_READ
                );

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

        // Ensure customer is registered in CRM if phone is provided
        if (StringUtils.hasText(request.getCustomerPhone())) {
            com.restaurant.crm.modules.crm.customer_account.entity.Customer customer = customerRepository.findByPhone(request.getCustomerPhone())
                    .orElseGet(() -> customerRepository.save(com.restaurant.crm.modules.crm.customer_account.entity.Customer.builder()
                            .phone(request.getCustomerPhone())
                            .status(com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus.ACTIVE)
                            .build()));
            pointWalletService.initializeWallet(customer.getId(), branchId);
        }

        // Update table status to OCCUPIED
        if (table != null) {
            table.setStatus(RestaurantTableStatus.OCCUPIED);
            restaurantTableRepository.save(table);
        }

        // Broadcast KDS update to kitchen
        sseEmitterService.broadcastToBranch(
                savedOrder.getBranchId(),
                "KDS_ORDER_CREATED",
                savedOrder.getId(),
                StartDefinedOrgPermission.ORDER_READ
        );

        return CreateOrderResponse.builder()
                .orderId(savedOrder.getId())
                .build();
    }


    @Override
    @Transactional
    public AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));
        validateOrderStatusForOrderItemMutation(existingOrder);

        BigDecimal unitPrice = resolveUnitPrice(existingOrder.getBranchId(), request.getProductId(), request.getComboId());

        OrderItem orderItem = OrderItem.builder()
                .order(existingOrder)
                .productId(request.getProductId())
                .comboId(request.getComboId())
                .quantity(request.getQuantity())
                .unitPrice(unitPrice)
                .subtotal(unitPrice.multiply(BigDecimal.valueOf(request.getQuantity())))
                .status(OrderItemStatus.PENDING)
                .note(request.getNote())
                .build();
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        BigDecimal modifierTotal = createOrderItemModifiers(savedOrderItem, request.getModifiers());

        savedOrderItem.setSubtotal(savedOrderItem.getSubtotal().add(modifierTotal));
        orderItemRepository.save(savedOrderItem);
        applyOrderSubtotalDelta(existingOrder, savedOrderItem.getSubtotal());

        // Broadcast KDS update to kitchen
        sseEmitterService.broadcastToBranch(
                existingOrder.getBranchId(),
                "KDS_ORDER_UPDATED",
                existingOrder.getId(),
                StartDefinedOrgPermission.ORDER_READ
        );

        return AddOrderItemResponse.builder()
                .orderItemId(savedOrderItem.getId())
                .build();
    }


    @Override
    @Transactional
    public void updateOrderItemQuantity(
            String orderId,
            String orderItemId,
            UpdateOrderItemQuantityRequestDto request
    ) {
        OrderItem existingOrderItem = getOrderItem(orderId, orderItemId);

        validateOrderStatusForOrderItemMutation(existingOrderItem.getOrder());
        validateOrderItemQuantityChange(existingOrderItem, request.getQuantity());

        if (request.getQuantity().equals(existingOrderItem.getQuantity())) {
            return;
        }

        BigDecimal quantityDelta = existingOrderItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity() - existingOrderItem.getQuantity()));
        existingOrderItem.setQuantity(request.getQuantity());
        existingOrderItem.setSubtotal(existingOrderItem.getSubtotal().add(quantityDelta));
        orderItemRepository.save(existingOrderItem);

        applyOrderSubtotalDelta(existingOrderItem.getOrder(), quantityDelta);

        // Broadcast KDS update to kitchen
        sseEmitterService.broadcastToBranch(
                existingOrderItem.getOrder().getBranchId(),
                "KDS_ITEM_UPDATED",
                existingOrderItem.getId(),
                StartDefinedOrgPermission.ORDER_READ
        );
    }


    @Override
    @Transactional
    public void updateOrderItemModifiers(
            String orderId,
            String orderItemId,
            UpdateOrderItemModifiersRequestDto request
    ) {
        OrderItem existingOrderItem = getOrderItem(orderId, orderItemId);
        List<OrderItemModifier> currentModifiers = orderItemModifierRepository.findAllByOrderItemId(orderItemId);

        validateOrderStatusForOrderItemMutation(existingOrderItem.getOrder());
        validateOrderItemStatusForModifierMutation(existingOrderItem);

        BigDecimal oldModifierSubtotal = calculateModifierTotal(currentModifiers);
        BigDecimal newModifierSubtotal = syncOrderItemModifiers(existingOrderItem, currentModifiers, request.getModifiers());
        BigDecimal modifierSubtotalDelta = newModifierSubtotal.subtract(oldModifierSubtotal);

        applyOrderItemSubtotalDelta(existingOrderItem, modifierSubtotalDelta);

        // Broadcast KDS update to kitchen
        sseEmitterService.broadcastToBranch(
                existingOrderItem.getOrder().getBranchId(),
                "KDS_ITEM_UPDATED",
                existingOrderItem.getId(),
                StartDefinedOrgPermission.ORDER_READ
        );
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

        // Broadcast KDS update to kitchen
        sseEmitterService.broadcastToBranch(
                order.getBranchId(),
                "KDS_ORDER_UPDATED",
                order.getId(),
                StartDefinedOrgPermission.ORDER_READ
        );

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
                .subtotal(order.getSubtotal())
                .discountAmount(order.getDiscountAmount())
                .totalAmount(order.getTotalAmount())
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

    private void validateOrderItemQuantityChange(OrderItem existingOrderItem, Integer requestedQuantity) {
        if (!requestedQuantity.equals(existingOrderItem.getQuantity())
                && !MODIFIABLE_ORDER_ITEM_STATUSES.contains(existingOrderItem.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ITEM_STATUS_NOT_MODIFIABLE);
        }
    }

    private void validateOrderItemStatusForModifierMutation(OrderItem existingOrderItem) {
        if (!MODIFIABLE_ORDER_ITEM_STATUSES.contains(existingOrderItem.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ITEM_STATUS_NOT_MODIFIABLE);
        }
    }

    private void validateOrderStatusForOrderItemMutation(Order order) {
        if (!MODIFIABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
    }

    private void validateOrderStatusForOrderCancellation(Order order) {
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new AppException(ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
    }

    private OrderItem getOrderItem(String orderId, String orderItemId) {
        return orderItemRepository.findByIdAndOrderIdWithOrder(orderItemId, orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));
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

    private BigDecimal createOrderItemModifiers(OrderItem orderItem, List<AddOrderItemModifierRequestDto> modifiers) {
        if (modifiers == null || modifiers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal modifierTotal = BigDecimal.ZERO;
        List<OrderItemModifier> modifiersToSave = new ArrayList<>();
        for (AddOrderItemModifierRequestDto modifierRequest : modifiers) {
            BigDecimal additionalPrice = resolveModifierAdditionalPrice(modifierRequest.getModifierOptionId());
            BigDecimal modifierSubtotal = calculateModifierSubtotal(additionalPrice, modifierRequest.getQuantity());
            modifierTotal = modifierTotal.add(modifierSubtotal);

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

    private BigDecimal resolveModifierAdditionalPrice(String modifierOptionId) {
        ModifierOption modifierOption = modifierOptionRepository.findById(modifierOptionId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_MODIFIER_OPTION_NOT_FOUND));
        return modifierOption.getAdditionalPrice();
    }

    private BigDecimal syncOrderItemModifiers(
            OrderItem orderItem,
            List<OrderItemModifier> currentModifiers,
            List<AddOrderItemModifierRequestDto> requestedModifiers
    ) {
        if (requestedModifiers == null) {
            requestedModifiers = List.of();
        }

        Map<String, OrderItemModifier> currentModifierMap = new HashMap<>();
        for (OrderItemModifier currentModifier : currentModifiers) {
            currentModifierMap.put(currentModifier.getModifierOptionId(), currentModifier);
        }

        Set<String> requestedModifierOptionIds = new HashSet<>();
        List<OrderItemModifier> modifiersToUpsert = new ArrayList<>();
        List<OrderItemModifier> modifiersToDelete = new ArrayList<>();
        BigDecimal newModifierSubtotal = BigDecimal.ZERO;

        for (AddOrderItemModifierRequestDto requestedModifier : requestedModifiers) {
            String modifierOptionId = requestedModifier.getModifierOptionId();
            requestedModifierOptionIds.add(modifierOptionId);

            BigDecimal additionalPrice = resolveModifierAdditionalPrice(modifierOptionId);
            newModifierSubtotal = newModifierSubtotal.add(
                    calculateModifierSubtotal(additionalPrice, requestedModifier.getQuantity())
            );

            OrderItemModifier existingModifier = currentModifierMap.get(modifierOptionId);
            if (existingModifier != null) {
                existingModifier.setAdditionalPrice(additionalPrice);
                existingModifier.setQuantity(requestedModifier.getQuantity());
                modifiersToUpsert.add(existingModifier);
                continue;
            }

            modifiersToUpsert.add(OrderItemModifier.builder()
                    .orderItem(orderItem)
                    .modifierOptionId(modifierOptionId)
                    .additionalPrice(additionalPrice)
                    .quantity(requestedModifier.getQuantity())
                    .build());
        }

        for (OrderItemModifier currentModifier : currentModifiers) {
            if (!requestedModifierOptionIds.contains(currentModifier.getModifierOptionId())) {
                modifiersToDelete.add(currentModifier);
            }
        }

        if (!modifiersToDelete.isEmpty()) {
            orderItemModifierRepository.deleteAll(modifiersToDelete);
        }
        if (!modifiersToUpsert.isEmpty()) {
            orderItemModifierRepository.saveAll(modifiersToUpsert);
        }

        return newModifierSubtotal;
    }

    private void applyOrderItemSubtotalDelta(OrderItem orderItem, BigDecimal subtotalDelta) {
        if (BigDecimal.ZERO.compareTo(subtotalDelta) == 0) {
            return;
        }

        orderItem.setSubtotal(orderItem.getSubtotal().add(subtotalDelta));
        orderItemRepository.save(orderItem);
        applyOrderSubtotalDelta(orderItem.getOrder(), subtotalDelta);
    }

    private void applyOrderSubtotalDelta(Order order, BigDecimal subtotalDelta) {
        if (BigDecimal.ZERO.compareTo(subtotalDelta) == 0) {
            return;
        }

        order.setSubtotal(order.getSubtotal().add(subtotalDelta));
        order.setTotalAmount(order.getSubtotal().subtract(order.getDiscountAmount()));
        orderRepository.save(order);
    }

    private BigDecimal calculateModifierSubtotal(BigDecimal additionalPrice, Integer quantity) {
        return additionalPrice.multiply(BigDecimal.valueOf(quantity));
    }

    private BigDecimal calculateModifierTotal(List<OrderItemModifier> modifiers) {
        return modifiers.stream()
                .map(modifier -> calculateModifierSubtotal(modifier.getAdditionalPrice(), modifier.getQuantity()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
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

    @Override
    public List<com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherApplicableResponse> getApplicableVouchers(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (!StringUtils.hasText(order.getCustomerPhone())) {
            return List.of();
        }

        com.restaurant.crm.modules.crm.customer_account.entity.Customer customer = customerRepository.findByPhone(order.getCustomerPhone())
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));

        return customerVoucherService.getApplicableVouchers(customer.getId(), order.getBranchId(), order.getSubtotal());
    }

    @Override
    @Transactional
    public void applyVoucher(String orderId, String customerVoucherId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        // Release any currently applied voucher on this order first
        customerVoucherService.releaseVoucher(orderId);

        // Apply the new voucher
        com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherResponse cv = customerVoucherService.useVoucher(customerVoucherId, orderId, order.getSubtotal());

        // Recalculate discount
        BigDecimal discountPercent = BigDecimal.valueOf(cv.getVoucher().getDiscountPercent());
        BigDecimal discountAmount = order.getSubtotal().multiply(discountPercent).divide(BigDecimal.valueOf(100));

        order.setDiscountAmount(discountAmount);
        order.setTotalAmount(order.getSubtotal().subtract(discountAmount));
        orderRepository.save(order);

        // Broadcast SSE update
        customerSseService.broadcastOrderUpdate(orderId, getOrderCookingStatus(orderId));
    }

    @Override
    @Transactional
    public void removeVoucher(String orderId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        if (order.getStatus() == OrderStatus.PAID) {
            throw new AppException(ErrorCode.ORDER_ALREADY_PAID);
        }

        customerVoucherService.releaseVoucher(orderId);

        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getSubtotal());
        orderRepository.save(order);

        // Broadcast SSE update
        customerSseService.broadcastOrderUpdate(orderId, getOrderCookingStatus(orderId));
    }
}
