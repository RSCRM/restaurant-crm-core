package com.restaurant.crm.modules.erp.order;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import com.restaurant.crm.modules.erp.menu.product.repository.ProductRepository;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
import com.restaurant.crm.modules.erp.notification.mapper.NotificationMapper;
import com.restaurant.crm.modules.erp.notification.service.interfaces.NotificationService;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemStatusRequest;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.mapper.OrderItemMapper;
import com.restaurant.crm.modules.erp.order.repository.OrderItemRepository;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.crm.loyalty_voucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.erp.order.service.impl.OrderItemServiceImpl;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
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
    CustomerSseService customerSseService;

    @Mock
    OrderItemMapper orderItemMapper;

    @Mock
    NotificationMapper notificationMapper;

    @Mock
    OrderRepository orderRepository;

    @Mock
    CustomerVoucherRepository customerVoucherRepository;

    @InjectMocks
    OrderItemServiceImpl orderItemService;

    @Test
    public void testUpdateStatus_InProgress_Success() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String employeeId = "chef-1";

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .order(order)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId("order-1")).thenReturn(Collections.singletonList(orderItem));

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.IN_PROGRESS)
                .build();
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        try (MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            authUtilsMockedStatic.when(AuthUtils::getEmployeeId).thenReturn(employeeId);

            OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.IN_PROGRESS).build());

            assertNotNull(response);
            assertEquals(OrderItemStatus.IN_PROGRESS, response.getStatus());
            assertEquals(employeeId, orderItem.getPreparedBy());
            verify(orderItemRepository, times(1)).save(orderItem);

            // Verify KDS broadcast is triggered for status change
            verify(sseEmitterService, times(1)).broadcastToBranch(
                    eq(branchId), eq("KDS_ITEM_UPDATED"), eq(itemId), any()
            );
            // Verify Customer SSE update is broadcasted
            verify(customerSseService, times(1)).broadcastOrderUpdate(eq("order-1"), any());
            // Verify no ready-to-serve notifications are triggered
            verify(notificationService, never()).create(any(), any(), any(), any(), any(), any());
        }
    }

    @Test
    public void testUpdateStatus_ReadyToServe_TriggersNotificationAndBroadcast() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String productId = "product-1";
        String tableId = "table-1";
        String chefId = "chef-1";

        TableArea area = TableArea.builder()
                .areaName("Khu A")
                .build();

        RestaurantTable table = RestaurantTable.builder()
                .tableNumber("Bàn 01")
                .area(area)
                .build();

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .tableId(tableId)
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .order(order)
                .productId(productId)
                .quantity(2)
                .status(OrderItemStatus.IN_PROGRESS)
                .preparedBy(chefId)
                .build();

        Product product = Product.builder()
                .productName("Phở Bò")
                .requiresPreparation(true)
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
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.singletonList(orderItem));

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

        try (MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            authUtilsMockedStatic.when(AuthUtils::getEmployeeId).thenReturn(chefId);

            // Execute status update to READY_TO_SERVE
            OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.READY_TO_SERVE).build());

            assertNotNull(response);
            assertEquals(OrderItemStatus.READY_TO_SERVE, response.getStatus());
            verify(orderItemRepository, times(1)).save(orderItem);
            // Verify notification is created in DB and pushed to SSE
            verify(notificationService, times(1)).create(any(), any(), any(), any(), any(), any());
            verify(sseEmitterService, times(1)).broadcastToBranch(eq(branchId), eq("READY_TO_SERVE"), eq(responseDto), any());
            // Verify customer SSE update is broadcasted
            verify(customerSseService, times(1)).broadcastOrderUpdate(any(), any());
        }
    }

    @Test
    public void testUpdateStatus_DoubleAccept_ThrowsAppException() {
        String itemId = "item-1";
        Order order = Order.builder().id("order-1").branchId("branch-1").build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.IN_PROGRESS)
                .order(order)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));

        AppException exception = assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.IN_PROGRESS).build());
        });

        assertEquals(ErrorCode.ORDER_ITEM_ALREADY_ACCEPTED, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatus_DirectServeDrink_Success() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String productId = "product-1";
        String employeeId = "waiter-1";

        Order order = Order.builder().id("order-1").branchId(branchId).build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .productId(productId)
                .order(order)
                .build();

        Product drink = Product.builder()
                .productName("Coca Cola")
                .requiresPreparation(false) // Drink does not require kitchen preparation
                .build();

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.READY_TO_SERVE)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(productId)).thenReturn(Optional.of(drink));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.singletonList(orderItem));
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        try (MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            authUtilsMockedStatic.when(AuthUtils::getEmployeeId).thenReturn(employeeId);

            OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.READY_TO_SERVE).build());

            assertNotNull(response);
            assertEquals(OrderItemStatus.READY_TO_SERVE, response.getStatus());
            assertEquals(employeeId, orderItem.getPreparedBy());
            verify(orderItemRepository, times(1)).save(orderItem);
        }
    }

    @Test
    public void testUpdateStatus_DirectServeFood_Blocked() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String productId = "product-1";

        Order order = Order.builder().id("order-1").branchId(branchId).build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .productId(productId)
                .order(order)
                .build();

        Product food = Product.builder()
                .productName("Phở Bò")
                .requiresPreparation(true) // Requires kitchen preparation
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(productRepository.findById(productId)).thenReturn(Optional.of(food));

        AppException exception = assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.READY_TO_SERVE).build());
        });

        assertEquals(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatus_ResponsibilityCheck_Failure() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String chefId = "chef-1";
        String otherChefId = "chef-2";

        Order order = Order.builder().id("order-1").branchId(branchId).build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.IN_PROGRESS)
                .preparedBy(chefId)
                .order(order)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));

        try (MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            authUtilsMockedStatic.when(AuthUtils::getEmployeeId).thenReturn(otherChefId);

            AppException exception = assertThrows(AppException.class, () -> {
                orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.READY_TO_SERVE).build());
            });

            assertEquals(ErrorCode.ORDER_ITEM_NOT_PREPARED_BY_YOU, exception.getErrorCode());
            verify(orderItemRepository, never()).save(any());
        }
    }

    @Test
    public void testUpdateStatus_CancelPreparation_Success() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String chefId = "chef-1";

        Order order = Order.builder().id("order-1").branchId(branchId).build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.IN_PROGRESS)
                .preparedBy(chefId)
                .order(order)
                .build();

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.PENDING)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.singletonList(orderItem));
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.PENDING).build());

        assertNotNull(response);
        assertEquals(OrderItemStatus.PENDING, response.getStatus());
        assertNull(orderItem.getPreparedBy());
        verify(orderItemRepository, times(1)).save(orderItem);
    }

    @Test
    public void testUpdateStatus_TerminalStateChange_Blocked() {
        String itemId = "item-1";
        Order order = Order.builder().id("order-1").branchId("branch-1").build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.SERVED)
                .order(order)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));

        AppException exception = assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.IN_PROGRESS).build());
        });

        assertEquals(ErrorCode.ORDER_ITEM_INVALID_STATUS_TRANSITION, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatus_ItemNotFound_ThrowsAppException() {
        String itemId = "non-existent";
        when(orderItemRepository.findById(itemId)).thenReturn(Optional.empty());

        assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.IN_PROGRESS).build());
        });

        verify(orderItemRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatus_CancelItem_RecalculatesOrderFinancials() {
        String itemId = "item-1";
        String branchId = "branch-1";

        Order order = Order.builder()
                .id("order-1")
                .branchId(branchId)
                .subtotal(BigDecimal.valueOf(150000))
                .discountAmount(BigDecimal.valueOf(15000))
                .totalAmount(BigDecimal.valueOf(135000))
                .build();

        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .subtotal(BigDecimal.valueOf(50000))
                .order(order)
                .build();

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.CANCELLED)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(customerVoucherRepository.findByOrderId("order-1")).thenReturn(Optional.empty());
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.singletonList(orderItem));
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.CANCELLED).reason("Out of stock").build());

        assertNotNull(response);
        assertEquals(OrderItemStatus.CANCELLED, response.getStatus());

        // original subtotal (150k) - cancelled item (50k) = 100k
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(order.getSubtotal()));
        // no voucher -> discount = 0
        assertEquals(0, BigDecimal.ZERO.compareTo(order.getDiscountAmount()));
        // total = 100k
        assertEquals(0, BigDecimal.valueOf(100000).compareTo(order.getTotalAmount()));

        verify(orderRepository, times(1)).save(order);
    }

    @Test
    public void testUpdateStatus_CancelItem_NoReason_ThrowsException() {
        String itemId = "item-1";
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));

        AppException exception = assertThrows(AppException.class, () -> {
            orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.CANCELLED).reason(null).build());
        });

        assertEquals(ErrorCode.ORDER_ITEM_CANCEL_REASON_REQUIRED, exception.getErrorCode());
        verify(orderItemRepository, never()).save(any());
    }

    @Test
    public void testUpdateStatus_CancelItem_WithReason_SavesAuditDetails() {
        String itemId = "item-1";
        String branchId = "branch-1";
        String employeeId = "chef-1";

        Order order = Order.builder().id("order-1").branchId(branchId).subtotal(BigDecimal.valueOf(100)).totalAmount(BigDecimal.valueOf(100)).build();
        OrderItem orderItem = OrderItem.builder()
                .id(itemId)
                .status(OrderItemStatus.PENDING)
                .subtotal(BigDecimal.valueOf(100))
                .order(order)
                .build();

        OrderItemResponse expectedResponse = OrderItemResponse.builder()
                .orderItemId(itemId)
                .status(OrderItemStatus.CANCELLED)
                .build();

        when(orderItemRepository.findById(itemId)).thenReturn(Optional.of(orderItem));
        when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> inv.getArgument(0));
        when(orderItemRepository.findByOrderId(any())).thenReturn(Collections.singletonList(orderItem));
        when(orderItemMapper.toOrderItemResponse(any(OrderItem.class))).thenReturn(expectedResponse);

        try (MockedStatic<AuthUtils> authUtilsMockedStatic = Mockito.mockStatic(AuthUtils.class)) {
            authUtilsMockedStatic.when(AuthUtils::getEmployeeId).thenReturn(employeeId);

            OrderItemResponse response = orderItemService.updateStatus(itemId, UpdateOrderItemStatusRequest.builder().status(OrderItemStatus.CANCELLED).reason("Out of stock").build());

            assertNotNull(response);
            assertEquals(OrderItemStatus.CANCELLED, response.getStatus());
            assertEquals("Out of stock", orderItem.getCancelReason());
            assertEquals(employeeId, orderItem.getCancelledBy());
        }
    }
}
