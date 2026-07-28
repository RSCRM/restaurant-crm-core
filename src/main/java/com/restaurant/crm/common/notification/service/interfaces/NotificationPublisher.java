package com.restaurant.crm.common.notification.service.interfaces;

import com.restaurant.crm.common.notification.dto.NotificationCommand;
import com.restaurant.crm.common.notification.entity.Notification;

/**
 * Write side of the shared notification infrastructure.
 * <p>
 * This is the internal API business modules call; it enforces tenant integrity but not the send
 * permission matrix, which only applies to notifications a human composes.
 */
public interface NotificationPublisher {

    /**
     * Persists one notification.
     * <p>
     * When the command carries a {@code dedupeKey} that already exists in the organization, the
     * existing row is returned untouched instead of a duplicate being written.
     *
     * @throws com.restaurant.crm.common.exception.AppException if the target fields do not match the scope
     */
    Notification publish(NotificationCommand command);
}