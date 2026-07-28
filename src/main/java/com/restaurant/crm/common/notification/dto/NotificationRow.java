package com.restaurant.crm.common.notification.dto;

import com.restaurant.crm.common.notification.entity.Notification;
import com.restaurant.crm.common.notification.enums.ReceiptStatus;

/**
 * One feed row: the notification plus this recipient's acknowledgement, if any.
 *
 * @param notification  the notification itself
 * @param receiptStatus null when the recipient has never acknowledged it, i.e. unread
 */
public record NotificationRow(Notification notification, ReceiptStatus receiptStatus) {
}