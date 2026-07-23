package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderType;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    OrganizationBranchRepository organizationBranchRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    RestaurantTableRepository restaurantTableRepository;

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

        if (request.getOrderType() == OrderType.DINE_IN) {
            if (!StringUtils.hasText(request.getTableId())) {
                throw new AppException(ErrorCode.ORDER_TABLE_ID_REQUIRED);
            }

            if (!restaurantTableRepository.existsByIdAndAreaBranchId(request.getTableId(), branchId)) {
                throw new AppException(ErrorCode.ORDER_TABLE_NOT_FOUND);
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

        return CreateOrderResponse.builder()
                .orderId(savedOrder.getId())
                .build();
    }

    private String generateOrderCode() {
        return OrderConstants.ORDER_CODE_PREFIX
                + UUID.randomUUID().toString().substring(0, OrderConstants.ORDER_CODE_RANDOM_LENGTH).toUpperCase();
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
}
