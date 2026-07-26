package com.restaurant.crm.modules.erp.order.enums;

/**
 * Lifecycle status of a QR ordering session (uc-c-02).
 * The session lives in Redis and is independent of the DB {@code Order}.
 * <ul>
 *   <li>{@link #OPEN} — members may join and add items.</li>
 *   <li>{@link #LOCKED_FOR_PAYMENT} — cart is frozen while payment is processed (BR-CST-PAY-01).</li>
 *   <li>{@link #CLOSED} — session finished (order PAID or manually closed); QR tokens are revoked.</li>
 * </ul>
 */
public enum QrSessionStatus {
    OPEN,
    LOCKED_FOR_PAYMENT,
    CLOSED
}
