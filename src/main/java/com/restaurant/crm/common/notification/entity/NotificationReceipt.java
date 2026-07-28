package com.restaurant.crm.common.notification.entity;

import com.restaurant.crm.common.notification.constants.NotificationConstants;
import com.restaurant.crm.common.notification.enums.ReceiptStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.IdClass;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Acknowledgement of one notification by one recipient.
 * <p>
 * The table is sparse on purpose: a row appears only once somebody actually reads or dismisses a
 * notification, so a branch-wide broadcast costs one insert instead of one row per employee.
 * A missing row means "unread".
 * <p>
 * Deliberately does <em>not</em> extend {@code BaseEntity}: this is a high-volume join table whose
 * natural key is the pair below. A surrogate UUID, an optimistic-lock version and audit columns
 * would cost storage and write time without buying anything — the row is written once and never
 * updated.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = NotificationConstants.TABLE_NOTIFICATION_RECEIPT)
@IdClass(NotificationReceiptId.class)
public class NotificationReceipt {

    @Id
    @Column(name = NotificationConstants.COL_NOTIFICATION_ID, nullable = false)
    String notificationId;

    @Id
    @Column(name = NotificationConstants.COL_RECIPIENT_ID, nullable = false)
    String recipientId;

    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_STATUS,
            nullable = false,
            length = NotificationConstants.MAX_CHARS_ENUM)
    ReceiptStatus status;

    @Column(name = NotificationConstants.COL_READ_AT)
    Instant readAt;
}