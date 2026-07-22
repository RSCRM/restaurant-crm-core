package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.ModifyOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import jakarta.persistence.EntityManager;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderServiceImpl implements OrderService {

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    EntityManager entityManager;

    @Override
    @Transactional
    public AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

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

        BigDecimal modifierTotal = replaceModifiers(savedOrderItem, request.getModifiers());

        savedOrderItem.setSubtotal(savedOrderItem.getSubtotal().add(modifierTotal));
        orderItemRepository.save(savedOrderItem);

        recalculateOrderTotals(existingOrder);

        return AddOrderItemResponse.builder()
                .orderItemId(savedOrderItem.getId())
                .build();
    }

    @Override
    @Transactional
    public void modifyOrderItem(String orderId, String orderItemId, ModifyOrderItemRequestDto request) {
        Order existingOrder = orderRepository.findById(orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_NOT_FOUND));

        OrderItem existingOrderItem = orderItemRepository.findByIdAndOrderId(orderItemId, orderId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        if (request.getComboId() != null) {
            resolveUnitPrice(existingOrder.getBranchId(), null, request.getComboId());
            existingOrderItem.setComboId(request.getComboId());
            existingOrderItem.setProductId(null);
        }

        if (request.getQuantity() != null) {
            existingOrderItem.setQuantity(request.getQuantity());
        }

        if (request.getNote() != null) {
            existingOrderItem.setNote(request.getNote());
        }

        existingOrderItem.setSubtotal(
                existingOrderItem.getUnitPrice().multiply(BigDecimal.valueOf(existingOrderItem.getQuantity()))
        );
        orderItemRepository.save(existingOrderItem);

        BigDecimal modifierTotal = replaceModifiers(existingOrderItem, request.getModifiers());
        existingOrderItem.setSubtotal(existingOrderItem.getSubtotal().add(modifierTotal));
        orderItemRepository.save(existingOrderItem);

        recalculateOrderTotals(existingOrder);
    }

    private BigDecimal resolveUnitPrice(String branchId, String productId, String comboId) {
        if (StringUtils.hasText(productId)) {
            // NOTE: Product validation/pricing currently reaches outside the order module
            // because product data is owned by another domain.
            List<?> productRows = entityManager.createNativeQuery(
                            """
                            select price
                            from products
                            where product_id = :productId
                              and branch_id = :branchId
                            """)
                    .setParameter("productId", productId)
                    .setParameter("branchId", branchId)
                    .getResultList();
            if (productRows.isEmpty()) {
                throw new AppException(ErrorCode.ORDER_PRODUCT_NOT_FOUND);
            }
            return toBigDecimal(productRows.get(0));
        }

        // NOTE: Combo validation/pricing also depends on data outside the current
        // order module boundary.
        List<?> comboRows = entityManager.createNativeQuery(
                        """
                        select price
                        from combos
                        where combo_id = :comboId
                          and branch_id = :branchId
                        """)
                .setParameter("comboId", comboId)
                .setParameter("branchId", branchId)
                .getResultList();
        if (comboRows.isEmpty()) {
            throw new AppException(ErrorCode.ORDER_COMBO_NOT_FOUND);
        }
        return toBigDecimal(comboRows.get(0));
    }

    private BigDecimal replaceModifiers(OrderItem orderItem, List<AddOrderItemModifierRequestDto> modifiers) {
        orderItemModifierRepository.deleteByOrderItemId(orderItem.getId());

        if (modifiers == null || modifiers.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal modifierTotal = BigDecimal.ZERO;
        List<OrderItemModifier> modifiersToSave = new ArrayList<>();
        for (AddOrderItemModifierRequestDto modifierRequest : modifiers) {
            // NOTE: Modifier option validation/pricing reaches outside the order module
            // because modifier catalogs are defined in another domain.
            List<?> modifierRows = entityManager.createNativeQuery(
                            "select additional_price from modifier_options where modifier_option_id = :modifierOptionId")
                    .setParameter("modifierOptionId", modifierRequest.getModifierOptionId())
                    .getResultList();
            if (modifierRows.isEmpty()) {
                throw new AppException(ErrorCode.ORDER_MODIFIER_OPTION_NOT_FOUND);
            }

            BigDecimal additionalPrice = toBigDecimal(modifierRows.get(0));
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

    private void recalculateOrderTotals(Order order) {
        BigDecimal subtotal = orderItemRepository.findAllByOrderId(order.getId()).stream()
                .map(OrderItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        order.setSubtotal(subtotal);
        order.setTotalAmount(subtotal.subtract(order.getDiscountAmount()));
        orderRepository.save(order);
    }

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }
}
