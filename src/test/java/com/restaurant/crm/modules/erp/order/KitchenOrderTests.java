package com.restaurant.crm.modules.erp.order;

import com.restaurant.crm.common.exception.AppException;
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
import com.restaurant.crm.modules.erp.order.service.impl.OrderItemServiceImpl;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for order item kitchen preparation flow and notifications.
 */
@ExtendWith(MockitoExtension.class)
public class KitchenOrderTests {

    @Mock
    OrderItemRepository orderItemRepository;

    @Mock
    ProductRepository productRepository;

    @Mock
    RestaurantTableRepository restaurantTableRepository;

    @Mock
    NotificationService notificationService;

    @Mock
    SseEmitterService sseEmitterService;

    @Mock
    OrderItemMapper orderItemMapper;

    @Mock
    NotificationMapper notificationMapper;

    @InjectMocks
    OrderItemServiceImpl orderItemService;

    @Test
    public void testUpdateStatus_InProgress_Success() {
        String itemId = "item-1";
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.IN_PROGRESS)
                .build();
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        OrderItemResponse response = orderItemService.updateStatus(itemId, OrderItemStatus.IN_PROGRESS);

        assertNotNull(response);
        assertEquals(OrderItemStatus.IN_PROGRESS, response.getStatus());
        verify(orderItemRepository, times(1)).save(orderItem);
        // Verify that no notification or SSE broadcast is triggered for IN_PROGRESS status
        verify(notificationService, never()).create(any(), any(), any(), any(), any(), any());
        verify(sseEmitterService, never()).broadcastToBranch(any(), any(), any(), any());
    }

    @Test
    public void testUpdateStatus_ReadyToServe_TriggersNotificationAndBroadcast() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String productId = "product-1";
        String tableId = "table-1";

        TableArea area = TableArea.builder()
                .areaName("Khu A")
                .build();

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber("Bàn 01")
                .area(area)
                .build();

        Order order = Order.builder()
                .branchId(branchId)
                .tableId(tableId)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .order(order)
                .productId(productId)
                .quantity(2)
                .status(OrderItemStatus.IN_PROGRESS)
                .build();

        Product product = Product.builder()
                .productName("Phở Bò")
                .build();

        Notification notification = Notification.builder()
                .branchId(branchId)
                .title("Dish Ready to Serve")
                .content("Khu A - Bàn 01: Phở Bò x2 is ready to serve!")
                .type(NotificationType.READY_TO_SERVE)
                .build();

        NotificationResponse responseDto = NotificationResponse.builder()
                .branchId(branchId)
                .title("Dish Ready to Serve")
                .content("Khu A - Bàn 01: Phở Bò x2 is ready to serve!")
                .type(NotificationType.READY_TO_SERVE)
                .build();

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.READY_TO_SERVE)
                .build();

        // Configure mock expectations
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(restaurantTableRepository.findById(tableId)).thenReturn(Optional.of(table));

        when(notificationService.create(
                eq(branchId),
                eq(null), // broadcast to all waiters
                any(),    // chef employee ID
                eq("Dish Ready to Serve"),
                eq("Khu A - Bàn 01: Phở Bò x2 is ready to serve!"),
                eq(NotificationType.READY_TO_SERVE)
        )).thenReturn(notification);

        when(notificationMapper.toNotificationResponse(notification)).thenReturn(responseDto);
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        // Execute status update to READY_TO_SERVE
        OrderItemResponse response = orderItemService.updateStatus(itemId, OrderItemStatus.READY_TO_SERVE);

        assertNotNull(response);
        assertEquals(OrderItemStatus.READY_TO_SERVE, response.getStatus());
        verify(orderItemRepository, times(1)).save(orderItem);
        // Verify notification is created in DB and pushed to SSE
        verify(notificationService, times(1)).create(any(), any(), any(), any(), any(), any());
        verify(sseEmitterService, times(1)).broadcastToBranch(eq(branchId), eq("READY_TO_SERVE"), eq(responseDto), eq(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission.ORDER_READ));
    }

    @Test
    public void testUpdateStatus_ItemNotFound_ThrowsAppException() {
        String itemId = "non-existent";
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, OrderItemStatus.IN_PROGRESS);
        });

        verify(orderItemRepository, never()).save(any());
        verify(notificationService, never()).create(any(), any(), any(), any(), any(), any());
        verify(sseEmitterService, never()).broadcastToBranch(any(), any(), any(), any());
    }
}
