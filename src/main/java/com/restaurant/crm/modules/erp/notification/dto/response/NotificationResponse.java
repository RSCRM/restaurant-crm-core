package com.restaurant.crm.modules.erp.notification.dto.response;

import com.restaurant.crm.modules.erp.notification.enums.NotificationStatus;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Data Transfer Object representing a notification response sent to the client.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationResponse {
    String id;
    String branchId;
    String recipientId;
    String senderId;
    String title;
    String content;
    NotificationType type;
    NotificationStatus status;
    Instant createdAt;
    Instant updatedAt;
}
