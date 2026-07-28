package com.restaurant.crm.common.notification.enums;

/**
 * Relative importance of a notification. Used by clients for ordering and styling, and reserved as
 * the shedding key when the delivery pipeline is saturated.
 */
public enum NotificationPriority {
    LOW,
    NORMAL,
    HIGH,
    CRITICAL
}