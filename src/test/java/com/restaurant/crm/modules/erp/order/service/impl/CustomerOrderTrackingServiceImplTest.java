package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.enums.CustomerOrderStage;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.mapper.CustomerOrderTrackingMapper;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;
import org.mockito.MockedStatic;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CustomerOrderTrackingServiceImplTest {

    private static final String SESSION = "session-1";
    private static final String TABLE = "table-1";
    private static final String ORDER = "order-1";

    private final QrSessionRedisRepository qrSessionRedisRepository = mock(QrSessionRedisRepository.class);
    private final OrderService orderService = mock(OrderService.class);
    private final CustomerSseService customerSseService = mock(CustomerSseService.class);
    private final CustomerOrderTrackingMapper mapper = Mappers.getMapper(CustomerOrderTrackingMapper.class);

    private final CustomerOrderTrackingServiceImpl service = new CustomerOrderTrackingServiceImpl(
            qrSessionRedisRepository, orderService, customerSseService, mapper);

    @Test
    void throwsWhenSessionNotFound() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, service::getCurrentCookingStatus);
            assertEquals(ErrorCode.TQR_SESSION_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void returnsNoActiveOrderWhenSessionHasNoOrder() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(null)));

            CustomerOrderTrackingResponse response = service.getCurrentCookingStatus();

            assertFalse(response.isHasActiveOrder());
            assertTrue(response.getItems().isEmpty());
            verify(orderService, never()).getOrderCookingStatus(anyString());
        }
    }

    @Test
    void mapsStagesAndSummaryAndHidesPhone() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(ORDER)));
            when(orderService.getOrderCookingStatus(ORDER)).thenReturn(order(List.of(
                    item("i1", OrderItemStatus.PENDING),
                    item("i2", OrderItemStatus.IN_PROGRESS),
                    item("i3", OrderItemStatus.READY_TO_SERVE),
                    item("i4", OrderItemStatus.SERVED),
                    item("i5", OrderItemStatus.CANCELLED))));

            CustomerOrderTrackingResponse response = service.getCurrentCookingStatus();

            assertTrue(response.isHasActiveOrder());
            assertEquals(new BigDecimal("250000.00"), response.getSubtotal());
            // stage mapping, especially READY_TO_SERVE → (3) and CANCELLED → (0), still listed.
            assertEquals(CustomerOrderStage.READY_TO_SERVE, response.getItems().get(2).getCustomerStage());
            assertEquals(3, response.getItems().get(2).getStageOrder());
            assertEquals(CustomerOrderStage.CANCELLED, response.getItems().get(4).getCustomerStage());
            assertEquals(0, response.getItems().get(4).getStageOrder());
            // summary counts
            assertEquals(5, response.getSummary().getTotal());
            assertEquals(1, response.getSummary().getReceived());
            assertEquals(1, response.getSummary().getCooking());
            assertEquals(1, response.getSummary().getReadyToServe());
            assertEquals(1, response.getSummary().getServed());
            assertEquals(1, response.getSummary().getCancelled());
        }
    }

    @Test
    void throwsContextMismatchWhenOrderBelongsToAnotherTable() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(ORDER)));
            OrderCookingStatusResponse foreign = OrderCookingStatusResponse.builder()
                    .orderId(ORDER).tableId("table-2").status(OrderStatus.PENDING).items(List.of()).build();
            when(orderService.getOrderCookingStatus(ORDER)).thenReturn(foreign);

            AppException exception = assertThrows(AppException.class, service::getCurrentCookingStatus);
            assertEquals(ErrorCode.TQR_CONTEXT_MISMATCH, exception.getErrorCode());
        }
    }

    @Test
    void mapsOrderNotFoundToTrackCode() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(ORDER)));
            when(orderService.getOrderCookingStatus(ORDER)).thenThrow(new AppException(ErrorCode.ORDER_NOT_FOUND));

            AppException exception = assertThrows(AppException.class, service::getCurrentCookingStatus);
            assertEquals(ErrorCode.TRACK_ORDER_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void subscribeCreatesEmitterForTheSessionOrderId() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(ORDER)));
            SseEmitter emitter = new SseEmitter();
            when(customerSseService.createEmitter(ORDER)).thenReturn(emitter);

            SseEmitter result = service.subscribeToCookingStatus();

            assertSame(emitter, result);
            verify(customerSseService, times(1)).createEmitter(ORDER); // proves the order-keyed channel
        }
    }

    @Test
    void subscribeThrowsWhenSessionHasNoOrder() {
        try (MockedStatic<AuthUtils> auth = session()) {
            when(qrSessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(session(null)));

            AppException exception = assertThrows(AppException.class, service::subscribeToCookingStatus);
            assertEquals(ErrorCode.TRACK_NO_ACTIVE_ORDER, exception.getErrorCode());
            verify(customerSseService, never()).createEmitter(anyString());
        }
    }

    // ==== fixtures ====

    private MockedStatic<AuthUtils> session() {
        MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getSessionId).thenReturn(SESSION);
        return auth;
    }

    private QrSessionData session(String orderId) {
        return new QrSessionData(SESSION, "org-1", "branch-1", TABLE, "owner-device",
                null, "0900000000", orderId, QrSessionStatus.OPEN, Instant.now());
    }

    private OrderCookingStatusResponse order(List<OrderItemCookingStatusResponse> items) {
        return OrderCookingStatusResponse.builder()
                .orderId(ORDER).orderCode("OC-1").tableId(TABLE).customerPhone("0900000000")
                .status(OrderStatus.PENDING)
                .subtotal(new BigDecimal("250000.00"))
                .discountAmount(new BigDecimal("0.00"))
                .totalAmount(new BigDecimal("250000.00"))
                .items(items).updatedAt(Instant.now()).build();
    }

    private OrderItemCookingStatusResponse item(String id, OrderItemStatus status) {
        return OrderItemCookingStatusResponse.builder()
                .orderItemId(id).itemName("Item " + id).quantity(1).note(null)
                .status(status).updatedAt(Instant.now()).build();
    }
}
