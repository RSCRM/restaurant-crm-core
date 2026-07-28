package com.restaurant.crm.common.notification.enums;

/**
 * Per-recipient acknowledgement state.
 * <p>
 * Absence of a receipt row means "unread" — receipts are stored sparsely so that broadcasting to a
 * branch stays a single insert regardless of headcount.
 */
public enum ReceiptStatus {
    READ,
    DISMISSED
}