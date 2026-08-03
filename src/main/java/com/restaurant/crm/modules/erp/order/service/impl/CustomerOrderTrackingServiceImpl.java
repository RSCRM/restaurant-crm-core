package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingSummaryResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.enums.CustomerOrderStage;
import com.restaurant.crm.modules.erp.order.mapper.CustomerOrderTrackingMapper;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerOrderTrackingService;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;
import java.util.Objects;

/**
 * uc-c-06 tracking service. Resolves the order id from the session, reuses
 * {@code OrderService.getOrderCookingStatus()} (no direct order/orderItem queries), strips the
 * owner phone, and enriches each line with the customer stage.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerOrderTrackingServiceImpl implements CustomerOrderTrackingService {

    QrSessionRedisRepository qrSessionRedisRepository;
    OrderService orderService;
    CustomerSseService customerSseService;
    CustomerOrderTrackingMapper customerOrderTrackingMapper;

    @Override
    @Transactional(readOnly = true)
    public CustomerOrderTrackingResponse getCurrentCookingStatus() {
        QrSessionData session = currentSession();
        String orderId = session.orderId();
        if (orderId == null || orderId.isBlank()) {
            return CustomerOrderTrackingResponse.builder()
                    .hasActiveOrder(false)
                    .items(List.of())
                    .build();
        }

        OrderCookingStatusResponse order = fetchOrder(orderId);
        // Branch isolation (NFR-07): the order must belong to the caller's table. The cooking-status
        // DTO exposes tableId (not branchId), and a table belongs to exactly one branch, so this is
        // the available and stronger check.
        if (!Objects.equals(order.getTableId(), session.tableId())) {
            throw new AppException(ErrorCode.TQR_CONTEXT_MISMATCH);
        }

        CustomerOrderTrackingResponse response = customerOrderTrackingMapper.toTracking(order);
        response.setHasActiveOrder(true);
        response.setSummary(buildSummary(response.getItems()));
        return response;
    }

    @Override
    public SseEmitter subscribeToCookingStatus() {
        QrSessionData session = currentSession();
        String orderId = session.orderId();
        if (orderId == null || orderId.isBlank()) {
            throw new AppException(ErrorCode.TRACK_NO_ACTIVE_ORDER);
        }
        // Reuse the order-keyed channel that OrderServiceImpl/OrderItemServiceImpl broadcast on.
        return customerSseService.createEmitter(orderId);
    }

    // ==== helpers ====

    private QrSessionData currentSession() {
        String sessionId = AuthUtils.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
        return qrSessionRedisRepository.findSession(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.TQR_SESSION_NOT_FOUND));
    }

    private OrderCookingStatusResponse fetchOrder(String orderId) {
        try {
            return orderService.getOrderCookingStatus(orderId);
        } catch (AppException exception) {
            // The session references an order that no longer exists / can't be read.
            throw new AppException(ErrorCode.TRACK_ORDER_NOT_FOUND);
        }
    }

    private CustomerOrderTrackingSummaryResponse buildSummary(List<CustomerOrderTrackingItemResponse> items) {
        int received = 0;
        int cooking = 0;
        int readyToServe = 0;
        int served = 0;
        int cancelled = 0;
        for (CustomerOrderTrackingItemResponse item : items) {
            CustomerOrderStage stage = item.getCustomerStage();
            if (stage == null) {
                continue;
            }
            switch (stage) {
                case RECEIVED -> received++;
                case COOKING -> cooking++;
                case READY_TO_SERVE -> readyToServe++;
                case SERVED -> served++;
                case CANCELLED -> cancelled++;
            }
        }
        return CustomerOrderTrackingSummaryResponse.builder()
                .total(items.size())
                .received(received)
                .cooking(cooking)
                .readyToServe(readyToServe)
                .served(served)
                .cancelled(cancelled)
                .build();
    }
}
