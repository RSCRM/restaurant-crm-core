package com.restaurant.crm.common.notification.dto;

import com.restaurant.crm.common.notification.enums.NotificationGroupType;
import com.restaurant.crm.common.notification.enums.NotificationPriority;
import com.restaurant.crm.common.notification.enums.NotificationScope;
import com.restaurant.crm.common.notification.enums.NotificationSenderType;
import com.restaurant.crm.common.notification.enums.NotificationType;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Request to emit one notification.
 * <p>
 * Business modules should start from {@link #from(NotificationType)} so the audience policy comes
 * from the type catalogue instead of being restated at each call site.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NotificationCommand {

    NotificationScope scope;

    /** Optional: resolved from {@link #branchId} when omitted. Null for SYSTEM scope. */
    String organizationId;

    String branchId;

    String targetKey;

    NotificationGroupType groupType;

    String recipientId;

    String senderId;

    NotificationSenderType senderType;

    String title;

    String content;

    NotificationType type;

    String requiredPermission;

    NotificationPriority priority;

    String payload;

    Instant expiresAt;

    /** Optional idempotency key, unique per organization. */
    String dedupeKey;

    /**
     * Pre-fills the audience policy from the type catalogue. The caller only supplies the tenant,
     * the sender and the text.
     */
    public static NotificationCommandBuilder from(NotificationType type) {
        return NotificationCommand.builder()
                .type(type)
                .scope(type.getDefaultScope())
                .groupType(type.getDefaultScope() == NotificationScope.GROUP ? type.getGroupType() : null)
                .targetKey(type.getDefaultTargetKey())
                .requiredPermission(type.getRequiredPermission())
                .priority(type.getPriority())
                .senderType(NotificationSenderType.SYSTEM);
    }
}