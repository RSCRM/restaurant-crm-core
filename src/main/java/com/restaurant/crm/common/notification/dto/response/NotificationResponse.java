package com.restaurant.crm.common.notification.dto.response;

import com.restaurant.crm.common.notification.enums.NotificationGroupType;
import com.restaurant.crm.common.notification.enums.NotificationPriority;
import com.restaurant.crm.common.notification.enums.NotificationScope;
import com.restaurant.crm.common.notification.enums.NotificationType;
import com.restaurant.crm.common.notification.enums.ReceiptStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * A notification as returned to the client.
 * <p>
 * {@code organizationId} is deliberately absent: the client already knows its own tenant and
 * echoing it back only widens what a mis-scoped response could leak.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {

    String id;
    NotificationScope scope;
    String branchId;
    String targetKey;
    NotificationGroupType groupType;
    String recipientId;
    String senderId;
    String title;
    String content;
    NotificationType type;
    NotificationPriority priority;
    String payload;
    Instant expiresAt;

    /** Null means unread — no receipt has been recorded for the calling employee. */
    ReceiptStatus readStatus;

    Instant createdAt;
    Instant updatedAt;
}