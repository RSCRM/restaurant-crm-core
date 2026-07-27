package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.modifier.entity.ModifierOption;
import com.restaurant.crm.modules.erp.menu.modifier.repository.ModifierOptionRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
import com.restaurant.crm.modules.erp.notification.mapper.NotificationMapper;
import com.restaurant.crm.modules.erp.notification.service.interfaces.NotificationService;
import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemModifierRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemStatusRequest;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.mapper.OrderItemMapper;
import com.restaurant.crm.modules.erp.order.repository.OrderItemModifierRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderItemService;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
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

/**
 * Service implementation for managing order items.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemServiceImpl implements OrderItemService {

    private static final Set<OrderStatus> MODIFIABLE_ORDER_STATUSES =
            EnumSet.of(OrderStatus.PENDING);
    private static final Set<OrderItemStatus> MODIFIABLE_ORDER_ITEM_STATUSES =
            EnumSet.of(OrderItemStatus.PENDING);

    OrderRepository orderRepository;
    OrderItemRepository orderItemRepository;
    OrderItemModifierRepository orderItemModifierRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    ModifierOptionRepository modifierOptionRepository;
    RestaurantTableRepository restaurantTableRepository;
    NotificationService notificationService;
    SseEmitterService sseEmitterService;
    CustomerSseService customerSseService;
    com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository customerVoucherRepository;
    OrderItemMapper orderItemMapper;
    NotificationMapper notificationMapper;

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
        broadcastStaffOrderUpdate(existingOrder);
        broadcastKdsItemEvent(existingOrder.getBranchId(), "KDS_ORDER_UPDATED", existingOrder.getId());

        return AddOrderItemResponse.builder()
                .orderItemId(savedOrderItem.getId())
                .build();
    }

    @Override
    @Transactional
    public void updateOrderItemQuantity(String orderId, String orderItemId, UpdateOrderItemQuantityRequestDto request) {
        OrderItem existingOrderItem = getOrderItem(orderId, orderItemId);

        validateOrderStatusForOrderItemMutation(existingOrderItem.getOrder());
        validateOrderItemQuantityChange(existingOrderItem, request.getQuantity());

        if (request.getQuantity().equals(existingOrderItem.getQuantity())) {
            return;
        }

        if (request.getQuantity() == OrderConstants.MIN_UPDATE_ORDER_ITEM_QUANTITY) {
            cancelOrderItem(existingOrderItem);
            return;
        }

        BigDecimal quantityDelta = existingOrderItem.getUnitPrice()
                .multiply(BigDecimal.valueOf(request.getQuantity() - existingOrderItem.getQuantity()));
        existingOrderItem.setQuantity(request.getQuantity());
        existingOrderItem.setSubtotal(existingOrderItem.getSubtotal().add(quantityDelta));
        orderItemRepository.save(existingOrderItem);

        applyOrderSubtotalDelta(existingOrderItem.getOrder(), quantityDelta);
        broadcastStaffOrderUpdate(existingOrderItem.getOrder());
        broadcastKdsItemEvent(existingOrderItem.getOrder().getBranchId(), "KDS_ITEM_UPDATED", existingOrderItem.getId());
    }

    @Override
    @Transactional
    public void updateOrderItemModifiers(String orderId, String orderItemId, UpdateOrderItemModifiersRequestDto request) {
        OrderItem existingOrderItem = getOrderItem(orderId, orderItemId);
        List<OrderItemModifier> currentModifiers = orderItemModifierRepository.findAllByOrderItemId(orderItemId);

        validateOrderStatusForOrderItemMutation(existingOrderItem.getOrder());
        validateOrderItemStatusForModifierMutation(existingOrderItem);

        BigDecimal oldModifierSubtotal = calculateModifierTotal(currentModifiers);
        BigDecimal newModifierSubtotal = syncOrderItemModifiers(existingOrderItem, currentModifiers, request.getModifiers());
        BigDecimal modifierSubtotalDelta = newModifierSubtotal.subtract(oldModifierSubtotal);

        applyOrderItemSubtotalDelta(existingOrderItem, modifierSubtotalDelta);
        broadcastStaffOrderUpdate(existingOrderItem.getOrder());
        broadcastKdsItemEvent(existingOrderItem.getOrder().getBranchId(), "KDS_ITEM_UPDATED", existingOrderItem.getId());
    }

    @Override
    @Transactional
    public void removeOrderItem(String orderId, String orderItemId) {
        OrderItem orderItem = getOrderItem(orderId, orderItemId);
        validateOrderStatusForOrderItemMutation(orderItem.getOrder());
        validateOrderItemStatusForModifierMutation(orderItem);
        cancelOrderItem(orderItem);
    }

    @Override
    @Transactional
    public OrderItemResponse updateStatus(String orderItemId, UpdateOrderItemStatusRequest request) {
        OrderItemStatus status = request.getStatus();
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseThrow(() -> new AppException(ErrorCode.ORDER_ITEM_NOT_FOUND));

        OrderItemStatus currentStatus = orderItem.getStatus();

        // 1. Terminal states check (SERVED, CANCELLED cannot transition to anything)
        if (currentStatus == OrderItemStatus.SERVED || currentStatus == OrderItemStatus.CANCELLED) {
            throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
        }

        // Retrieve product requires_preparation property
        boolean requiresPrep = true;
        if (orderItem.getProductId() != null) {
            Product product = productRepository.findById(orderItem.getProductId()).orElse(null);
            if (product != null && product.getRequiresPreparation() != null) {
                requiresPrep = product.getRequiresPreparation();
            }
        }

        orderItem.setStatus(status);

        if (status == OrderItemStatus.IN_PROGRESS) {
            try {
                String chefId = AuthUtils.getEmployeeId();
                if (chefId != null) {
                    orderItem.setPreparedBy(chefId);
                }
            } catch (Exception e) {
                // Fallback for tests/unauthenticated
            }
        }

        // Get current employee ID
        String currentEmployeeId = null;
        try {
            currentEmployeeId = AuthUtils.getEmployeeId();
        } catch (Exception e) {
            // Fallback for tests/unauthenticated
        }

        // 2. Validate transitions
        if (status == OrderItemStatus.IN_PROGRESS) {
            if (currentStatus == OrderItemStatus.IN_PROGRESS) {
                throw new AppException(ErrorCode.ORDER_ITEM_ALREADY_ACCEPTED);
            }
            if (currentStatus != OrderItemStatus.PENDING) {
                throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
            }
            if (currentEmployeeId != null) {
                orderItem.setPreparedBy(currentEmployeeId);
            }
        } else if (status == OrderItemStatus.READY_TO_SERVE) {
            if (currentStatus == OrderItemStatus.IN_PROGRESS) {
                if (currentEmployeeId != null && orderItem.getPreparedBy() != null && !currentEmployeeId.equals(orderItem.getPreparedBy())) {
                    throw new AppException(ErrorCode.ORDER_ITEM_NOT_PREPARED_BY_YOU);
                }
            } else if (currentStatus == OrderItemStatus.PENDING) {
                if (requiresPrep) {
                    throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
                }
                if (currentEmployeeId != null) {
                    orderItem.setPreparedBy(currentEmployeeId);
                }
            } else {
                throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
            }
        } else if (status == OrderItemStatus.PENDING) {
            if (currentStatus != OrderItemStatus.IN_PROGRESS) {
                throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
            }
            orderItem.setPreparedBy(null);
        } else if (status == OrderItemStatus.CANCELLED) {
            if (currentStatus != OrderItemStatus.PENDING && currentStatus != OrderItemStatus.IN_PROGRESS) {
                throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
            }
            if (!StringUtils.hasText(request.getReason())) {
                throw new AppException(ErrorCode.ORDER_ITEM_CANCEL_REASON_REQUIRED);
            }
            orderItem.setPreparedBy(null);
            orderItem.setCancelReason(request.getReason());
            if (currentEmployeeId != null) {
                orderItem.setCancelledBy(currentEmployeeId);
            }

            // Recalculate financials (Phương án A: Đầu bếp hủy món)
            Order order = orderItem.getOrder();
            BigDecimal updatedSubtotal = order.getSubtotal().subtract(orderItem.getSubtotal());
            order.setSubtotal(updatedSubtotal.max(BigDecimal.ZERO));
            recalculateOrderFinancials(order);
            orderRepository.save(order);
        } else if (status == OrderItemStatus.SERVED) {
            if (currentStatus != OrderItemStatus.READY_TO_SERVE) {
                throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
            }
        } else {
            throw new AppException(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION);
        }

        // Update the item status
        orderItem.setStatus(status);

        OrderItem savedItem = orderItemRepository.save(orderItem);

        if (status == OrderItemStatus.READY_TO_SERVE) {
            triggerReadyToServeNotification(savedItem);
        }

        // Broadcast cooking status update to customer SSE subscribers
        broadcastKdsItemEvent(savedItem.getOrder().getBranchId(), "KDS_ITEM_UPDATED", savedItem.getId());
        broadcastOrderCookingStatus(savedItem.getOrder());

        return orderItemMapper.toOrderItemResponse(savedItem);
    }

    private void triggerReadyToServeNotification(OrderItem item) {
        Order order = item.getOrder();
        String branchId = order.getBranchId();

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

        String location = "Take Out";
        if (order.getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
            if (table != null && table.getArea() != null) {
                location = String.format("%s - %s", table.getArea().getAreaName(), table.getTableNumber());
            } else if (table != null) {
                location = table.getTableNumber();
            }
        }

        String title = "Dish Ready to Serve";
        String content = String.format("%s: %s x%d is ready to serve!", location, itemName, item.getQuantity());

        String chefEmployeeId = "f0000000-0000-0000-0000-000000000008";
        try {
            String currentEmployeeId = AuthUtils.getEmployeeId();
            if (currentEmployeeId != null) {
                chefEmployeeId = currentEmployeeId;
            }
        } catch (Exception e) {
            // Use default fallback ID during testing/unauthenticated scenarios
        }

        Notification notification = notificationService.create(
                branchId,
                null,
                chefEmployeeId,
                title,
                content,
                NotificationType.READY_TO_SERVE
        );

        NotificationResponse responseDto = notificationMapper.toNotificationResponse(notification);
        sseEmitterService.broadcastToBranch(branchId, "READY_TO_SERVE", responseDto, StartDefinedOrgPermission.ORDER_READ);
    }

    private void broadcastOrderCookingStatus(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }

        customerSseService.broadcastOrderUpdate(order.getId(), buildOrderCookingStatusResponse(order));
    }

    private void validateOrderStatusForOrderItemMutation(Order order) {
        if (!MODIFIABLE_ORDER_STATUSES.contains(order.getStatus())) {
            throw new AppException(ErrorCode.ORDER_STATUS_NOT_MODIFIABLE);
        }
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

    private void recalculateOrderFinancials(Order order) {
        com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher appliedVoucher =
                customerVoucherRepository.findByOrderId(order.getId()).orElse(null);

        if (appliedVoucher != null) {
            BigDecimal discountPercent = BigDecimal.valueOf(appliedVoucher.getVoucher().getDiscountPercent());
            BigDecimal discountAmount = order.getSubtotal()
                    .multiply(discountPercent)
                    .divide(BigDecimal.valueOf(100));
            order.setDiscountAmount(discountAmount);
            order.setTotalAmount(order.getSubtotal().subtract(discountAmount));
            return;
        }

        order.setDiscountAmount(BigDecimal.ZERO);
        order.setTotalAmount(order.getSubtotal());
    }

    private void cancelOrderItem(OrderItem orderItem) {
        Order order = orderItem.getOrder();

        orderItem.setStatus(OrderItemStatus.CANCELLED);
        orderItemRepository.save(orderItem);

        BigDecimal updatedSubtotal = order.getSubtotal().subtract(orderItem.getSubtotal());
        order.setSubtotal(updatedSubtotal.max(BigDecimal.ZERO));
        recalculateOrderFinancials(order);
        orderRepository.save(order);

        broadcastStaffOrderUpdate(order);
        broadcastKdsItemEvent(order.getBranchId(), "KDS_ITEM_UPDATED", orderItem.getId());
    }

    private void broadcastStaffOrderUpdate(Order order) {
        if (order == null || !StringUtils.hasText(order.getBranchId())) {
            return;
        }

        sseEmitterService.broadcastToBranch(
                order.getBranchId(),
                "ORDER_UPDATED",
                buildOrderCookingStatusResponse(order),
                StartDefinedOrgPermission.ORDER_READ
        );
    }

    private void broadcastKdsItemEvent(String branchId, String eventName, String itemId) {
        if (!StringUtils.hasText(branchId)) {
            return;
        }

        sseEmitterService.broadcastToBranch(
                branchId,
                eventName,
                itemId,
                StartDefinedOrgPermission.ORDER_READ
        );
    }

    private OrderCookingStatusResponse buildOrderCookingStatusResponse(Order order) {
        List<OrderItem> items = orderItemRepository.findByOrderId(order.getId());
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
}
