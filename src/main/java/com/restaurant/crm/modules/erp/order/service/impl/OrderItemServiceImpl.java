package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.menu.combo.entity.Combo;
import com.restaurant.crm.modules.erp.menu.combo.repository.ComboRepository;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
import com.restaurant.crm.modules.erp.notification.mapper.NotificationMapper;
import com.restaurant.crm.modules.erp.notification.service.interfaces.NotificationService;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.mapper.OrderItemMapper;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderItemService;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemCookingStatusResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * Service implementation for managing order items.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemServiceImpl implements OrderItemService {

    OrderItemRepository orderItemRepository;
    ProductRepository productRepository;
    ComboRepository comboRepository;
    RestaurantTableRepository restaurantTableRepository;
    NotificationService notificationService;
    SseEmitterService sseEmitterService;
    CustomerSseService customerSseService;
    OrderItemMapper orderItemMapper;
    NotificationMapper notificationMapper;

    @Override
    @Transactional
    public OrderItemResponse updateStatus(String orderItemId, OrderItemStatus status) {
        // Retrieve the OrderItem from database
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
            orderItem.setPreparedBy(null);
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

        // If the item status becomes READY_TO_SERVE, trigger SSE notification and persist it
        if (status == OrderItemStatus.READY_TO_SERVE) {
            triggerReadyToServeNotification(savedItem);
        }

        // Broadcast KDS update to kitchen displays in branch
        sseEmitterService.broadcastToBranch(
                savedItem.getOrder().getBranchId(),
                "KDS_ITEM_UPDATED",
                savedItem.getId(),
                StartDefinedOrgPermission.ORDER_READ
            );

        // Broadcast cooking status update to customer SSE subscribers
        broadcastOrderCookingStatus(savedItem.getOrder());

        return orderItemMapper.toOrderItemResponse(savedItem);
    }


    /**
     * Formats notification content, saves it to the database, and broadcasts it via SSE.
     *
     * @param item the completed OrderItem
     */
    private void triggerReadyToServeNotification(OrderItem item) {
        Order order = item.getOrder();
        String branchId = order.getBranchId();

        // Resolve item name (either Product name or Combo name)
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

        // Resolve table/location details
        String location = "Take Out";
        if (order.getTableId() != null) {
            RestaurantTable table = restaurantTableRepository.findById(order.getTableId()).orElse(null);
            if (table != null && table.getArea() != null) {
                location = String.format("%s - %s", table.getArea().getAreaName(), table.getTableNumber());
            } else if (table != null) {
                location = table.getTableNumber();
            }
        }

        // Format notification message details
        String title = "Dish Ready to Serve";
        String content = String.format("%s: %s x%d is ready to serve!", location, itemName, item.getQuantity());

        // Resolve sender ID (current chef employee ID). Fallback to standard test chef ID if unauthenticated.
        String chefEmployeeId = "f0000000-0000-0000-0000-000000000008";
        try {
            String currentEmployeeId = AuthUtils.getEmployeeId();
            if (currentEmployeeId != null) {
                chefEmployeeId = currentEmployeeId;
            }
        } catch (Exception e) {
            // Use default fallback ID during testing/unauthenticated scenarios
        }

        // Create and persist the notification
        Notification notification = notificationService.create(
                branchId,
                null, // Broadcast to all waiters in branch, so recipientId is null
                chefEmployeeId,
                title,
                content,
                NotificationType.READY_TO_SERVE
        );

        // Map notification to DTO and broadcast it via SSE
        NotificationResponse responseDto = notificationMapper.toNotificationResponse(notification);
        sseEmitterService.broadcastToBranch(branchId, "READY_TO_SERVE", responseDto, StartDefinedOrgPermission.ORDER_READ);
    }

    private void broadcastOrderCookingStatus(Order order) {
        if (order == null || order.getId() == null) {
            return;
        }
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

        OrderCookingStatusResponse statusResponse = OrderCookingStatusResponse.builder()
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

        customerSseService.broadcastOrderUpdate(order.getId(), statusResponse);
    }
}
