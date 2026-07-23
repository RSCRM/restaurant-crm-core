package com.restaurant.crm.modules.erp.notification.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.notification.constants.NotificationConstants;
import com.restaurant.crm.modules.erp.notification.enums.NotificationStatus;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;
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

/**
 * Entity representing a notification in the system.
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

    @NotBlank
    @NotNull
    @Column(name = NotificationConstants.COL_BRANCH_ID, nullable = false)
    String branchId;

    @Column(name = NotificationConstants.COL_RECIPIENT_ID)
    String recipientId;

    @NotBlank
    @NotNull
    @Column(name = NotificationConstants.COL_SENDER_ID, nullable = false)
    String senderId;

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
    @Column(name = NotificationConstants.COL_TYPE, nullable = false)
    NotificationType type;

    @Builder.Default
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = NotificationConstants.COL_STATUS, nullable = false)
    NotificationStatus status = NotificationStatus.UNREAD;
}
