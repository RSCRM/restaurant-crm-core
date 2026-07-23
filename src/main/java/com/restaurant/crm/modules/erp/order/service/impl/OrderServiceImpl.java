package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    private static final Set<OrderStatus> MODIFIABLE_ORDER_STATUSES =
            EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);
    private static final Set<OrderStatus> MODIFIABLE_ORDER_ITEM_STATUSES =
            EnumSet.of(OrderStatus.PENDING, OrderStatus.CONFIRMED);

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;

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
                .note(request.getNote())
                .build();
        OrderItem savedOrderItem = orderItemRepository.save(orderItem);

        BigDecimal modifierTotal = createOrderItemModifiers(savedOrderItem, request.getModifiers());

        savedOrderItem.setSubtotal(savedOrderItem.getSubtotal().add(modifierTotal));
        orderItemRepository.save(savedOrderItem);
        applyOrderSubtotalDelta(existingOrder, savedOrderItem.getSubtotal());

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
    }

    private void validateOrderItemQuantityChange(OrderItem existingOrderItem, Integer requestedQuantity) {
        if (!requestedQuantity.equals(existingOrderItem.getQuantity())
                && !MODIFIABLE_ORDER_ITEM_STATUSES.contains(existingOrderItem.getStatus())) {
            throw new AppException(ErrorCode.ORDER_ITEM_STATUS_NOT_MODIFIABLE);
        }
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

    private BigDecimal resolveModifierAdditionalPrice(String modifierOptionId) {
        ModifierOption modifierOption = modifierOptionRepository.findById(modifierOptionId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_MODIFIER_OPTION_NOT_FOUND));
        return modifierOption.getAdditionalPrice();
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
}
