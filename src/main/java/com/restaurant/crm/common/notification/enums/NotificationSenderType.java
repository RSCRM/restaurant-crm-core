package com.restaurant.crm.common.notification.enums;

/**
 * Origin of a notification.
 * <p>
 * The distinction matters for authorization: {@link #USER} notifications go through the send
 * permission matrix, {@link #SYSTEM} ones are emitted by business modules reacting to an event and
 * only carry a tenant context.
 */
public enum NotificationSenderType {

    /** Emitted by a logged-in employee. {@code sender_id} holds their employeeId. */
    USER,

    /** Emitted by a business module. {@code sender_id} may be null. */
    SYSTEM
}