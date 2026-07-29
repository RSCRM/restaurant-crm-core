package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Session-scoped, customer-facing cooking progress tracking (uc-c-06).
 * The order id always comes from the session ({@code QrSessionData.orderId}), never from the client;
 * the payload never carries the owner's phone or the kitchen's cancel reason.
 */
public interface CustomerOrderTrackingService {

    /**
     * Returns the current order's cooking progress for the caller's session (uc-c-06).
     * If the session has no order yet, {@code hasActiveOrder = false} with empty items (not an error).
     */
    CustomerOrderTrackingResponse getCurrentCookingStatus();

    /**
     * Opens an SSE stream for the session's active order (uc-c-06), reusing {@code CustomerSseService}
     * keyed by {@code orderId} so kitchen status broadcasts reach the customer.
     * Throws {@code TRACK_NO_ACTIVE_ORDER} if the session has not submitted an order yet.
     */
    SseEmitter subscribeToCookingStatus();
}
