package com.restaurant.crm.common.notification.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.common.notification.constants.NotificationConstants;
import com.restaurant.crm.common.notification.enums.NotificationGroupType;
import com.restaurant.crm.common.notification.enums.NotificationPriority;
import com.restaurant.crm.common.notification.enums.NotificationScope;
import com.restaurant.crm.common.notification.enums.NotificationSenderType;
import com.restaurant.crm.common.notification.enums.NotificationType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Table;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

/**
 * A single notification. One row per logical notification regardless of how many people receive it
 * (fan-out on read) — broadcasting to a branch of fifty is one insert, not fifty.
 * <p>
 * Read state deliberately does <strong>not</strong> live here: it belongs to the
 * (notification, recipient) pair and is stored in {@link NotificationReceipt}.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = NotificationConstants.TABLE_NOTIFICATION)
public class Notification extends BaseEntity {

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_SCOPE,
            nullable = false,
            length = NotificationConstants.MAX_CHARS_ENUM)
    NotificationScope scope;

    /**
     * Tenant root. Null only for {@link NotificationScope#SYSTEM}. Leading column of every index and
     * of every query — no read path filters on branch alone.
     */
    @Column(name = NotificationConstants.COL_ORGANIZATION_ID)
    String organizationId;

    /** Null for {@link NotificationScope#SYSTEM}. */
    @Column(name = NotificationConstants.COL_BRANCH_ID)
    String branchId;

    /** Role name or permission name, depending on {@link #groupType}. Only for GROUP scope. */
    @Size(max = NotificationConstants.MAX_CHARS_KEY)
    @Column(name = NotificationConstants.COL_TARGET_KEY,
            length = NotificationConstants.MAX_CHARS_KEY)
    String targetKey;

    /** Only for GROUP scope. */
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_GROUP_TYPE,
            length = NotificationConstants.MAX_CHARS_ENUM)
    NotificationGroupType groupType;

    /** Only for DIRECT scope. */
    @Column(name = NotificationConstants.COL_RECIPIENT_ID)
    String recipientId;

    /** employeeId of the sender, or null when {@link #senderType} is SYSTEM. */
    @Column(name = NotificationConstants.COL_SENDER_ID)
    String senderId;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_SENDER_TYPE,
            nullable = false,
            length = NotificationConstants.MAX_CHARS_ENUM)
    NotificationSenderType senderType;

    @NotBlank
    @NotNull
    @Size(max = NotificationConstants.MAX_CHARS_TITLE)
    @Column(name = NotificationConstants.COL_TITLE, nullable = false)
    String title;

    @NotBlank
    @NotNull
    @Size(max = NotificationConstants.MAX_CHARS_CONTENT)
    @Column(name = NotificationConstants.COL_CONTENT, nullable = false)
    String content;

    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_TYPE,
            nullable = false,
            length = NotificationConstants.MAX_CHARS_TYPE)
    NotificationType type;

    /**
     * Minimum authority required to see this row, snapshotted at write time so that the push path
     * and the read path can never disagree about it. Null means "everyone inside the scope".
     */
    @Size(max = NotificationConstants.MAX_CHARS_KEY)
    @Column(name = NotificationConstants.COL_REQUIRED_PERMISSION,
            length = NotificationConstants.MAX_CHARS_KEY)
    String requiredPermission;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_PRIORITY,
            nullable = false,
            length = NotificationConstants.MAX_CHARS_ENUM)
    NotificationPriority priority = NotificationPriority.NORMAL;

    /** Free-form JSON for deep links and entity references (orderId, tableId, ...). */
    @Column(name = NotificationConstants.COL_PAYLOAD,
            columnDefinition = NotificationConstants.PAYLOAD_DEFINITION)
    String payload;

    /** Past this instant the row is hidden from every feed and becomes eligible for cleanup. */
    @Column(name = NotificationConstants.COL_EXPIRES_AT)
    Instant expiresAt;

    /** Idempotency key for at-least-once emitters, unique per organization when present. */
    @Size(max = NotificationConstants.MAX_CHARS_DEDUPE_KEY)
    @Column(name = NotificationConstants.COL_DEDUPE_KEY,
            length = NotificationConstants.MAX_CHARS_DEDUPE_KEY)
    String dedupeKey;
}