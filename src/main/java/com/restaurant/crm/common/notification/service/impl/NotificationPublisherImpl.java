package com.restaurant.crm.common.notification.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.notification.dto.NotificationCommand;
import com.restaurant.crm.common.notification.entity.Notification;
import com.restaurant.crm.common.notification.enums.NotificationPriority;
import com.restaurant.crm.common.notification.enums.NotificationScope;
import com.restaurant.crm.common.notification.enums.NotificationSenderType;
import com.restaurant.crm.common.notification.repository.NotificationRepository;
import com.restaurant.crm.common.notification.service.interfaces.BranchTenantResolver;
import com.restaurant.crm.common.notification.service.interfaces.NotificationPublisher;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Persists notifications after checking that their target fields match their scope.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationPublisherImpl implements NotificationPublisher {

    NotificationRepository notificationRepository;
    BranchTenantResolver branchTenantResolver;

    @Override
    @Transactional
    public Notification publish(NotificationCommand command) {
        validateContent(command);

        NotificationScope scope = command.getScope();
        if (scope == null) {
            throw new AppException(ErrorCode.NOTIFICATION_INVALID_TARGET);
        }

        String organizationId = resolveOrganizationId(command, scope);
        validateTarget(command, scope, organizationId);

        // Retry of an at-least-once emitter: hand back what was already written.
        if (command.getDedupeKey() != null) {
            Optional<Notification> existing = notificationRepository
                    .findByOrganizationIdAndDedupeKey(organizationId, command.getDedupeKey());
            if (existing.isPresent()) {
                return existing.get();
            }
        }

        Notification notification = Notification.builder()
                .scope(scope)
                .organizationId(organizationId)
                .branchId(command.getBranchId())
                .targetKey(command.getTargetKey())
                .groupType(command.getGroupType())
                .recipientId(command.getRecipientId())
                .senderId(command.getSenderId())
                .senderType(command.getSenderType() == null
                        ? NotificationSenderType.SYSTEM
                        : command.getSenderType())
                .title(command.getTitle())
                .content(command.getContent())
                .type(command.getType())
                .requiredPermission(command.getRequiredPermission())
                .priority(command.getPriority() == null
                        ? NotificationPriority.NORMAL
                        : command.getPriority())
                .payload(command.getPayload())
                .expiresAt(command.getExpiresAt())
                .dedupeKey(command.getDedupeKey())
                .build();

        return notificationRepository.save(notification);
    }

    private void validateContent(NotificationCommand command) {
        if (command == null
                || command.getType() == null
                || isBlank(command.getTitle())
                || isBlank(command.getContent())) {
            throw new AppException(ErrorCode.NOTIFICATION_INVALID_TARGET);
        }
    }

    /**
     * SYSTEM notifications have no tenant by definition. For every other scope the organization is
     * taken from the command when the caller already knows it (the common case, no extra read), and
     * resolved from the branch otherwise.
     */
    private String resolveOrganizationId(NotificationCommand command, NotificationScope scope) {
        if (scope == NotificationScope.SYSTEM) {
            // Returned as-is rather than coerced to null: a SYSTEM command that carries a tenant is
            // a mistake, and silently stripping it would turn it into a platform-wide broadcast.
            // validateTarget rejects it.
            return command.getOrganizationId();
        }
        if (command.getOrganizationId() != null) {
            return command.getOrganizationId();
        }
        if (command.getBranchId() == null) {
            throw new AppException(ErrorCode.NOTIFICATION_INVALID_TARGET);
        }
        return branchTenantResolver.resolveOrganizationId(command.getBranchId());
    }

    /**
     * Mirrors the {@code ck_notifications_scope_target} database constraint. Enforcing it here as
     * well turns a constraint violation deep inside a business transaction into a clear error at the
     * call site.
     */
    private void validateTarget(NotificationCommand command, NotificationScope scope, String organizationId) {
        boolean valid = switch (scope) {
            case SYSTEM -> organizationId == null
                    && command.getBranchId() == null
                    && command.getRecipientId() == null
                    && command.getTargetKey() == null;
            case BRANCH -> organizationId != null
                    && command.getBranchId() != null;
            case GROUP -> organizationId != null
                    && command.getBranchId() != null
                    && command.getTargetKey() != null
                    && command.getGroupType() != null;
            case DIRECT -> organizationId != null
                    && command.getBranchId() != null
                    && command.getRecipientId() != null;
        };

        if (!valid) {
            throw new AppException(ErrorCode.NOTIFICATION_INVALID_TARGET);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}