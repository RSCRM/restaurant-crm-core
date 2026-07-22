package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
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
import java.util.UUID;

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
    public CreateOrderResponse create(String branchId, CreateOrderRequestDto request) {
        if (!StringUtils.hasText(branchId)) {
            throw new AppException(ErrorCode.ORDER_BRANCH_ID_REQUIRED);
        }

        // NOTE: Branch validation currently reaches outside the order module because
        // a dedicated branch domain/repository has not been introduced yet.
        Number branchCount = (Number) entityManager.createNativeQuery(
                        "select count(1) from organization_branches where branch_id = :branchId")
                .setParameter("branchId", branchId)
                .getSingleResult();
        if (branchCount.longValue() == 0) {
            throw new AppException(ErrorCode.ORDER_BRANCH_NOT_FOUND);
        }

        if (request.getOrderType() == OrderType.DINE_IN) {
            if (!StringUtils.hasText(request.getTableId())) {
                throw new AppException(ErrorCode.ORDER_TABLE_ID_REQUIRED);
            }

            // NOTE: Table validation also depends on tables/areas data that lives
            // outside the current order module boundary.
            Number tableCount = (Number) entityManager.createNativeQuery(
                            """
                            select count(1)
                            from restaurant_tables rt
                            join table_areas ta on ta.area_id = rt.area_id
                            where rt.table_id = :tableId
                              and ta.branch_id = :branchId
                            """)
                    .setParameter("tableId", request.getTableId())
                    .setParameter("branchId", branchId)
                    .getSingleResult();
            if (tableCount.longValue() == 0) {
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

    private BigDecimal toBigDecimal(Object value) {
        if (value instanceof BigDecimal bigDecimal) {
            return bigDecimal;
        }
        return new BigDecimal(value.toString());
    }
}
