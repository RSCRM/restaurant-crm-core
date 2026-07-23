package com.restaurant.crm.modules.erp.notification.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.entity.Notification;
import com.restaurant.crm.modules.erp.notification.enums.NotificationType;

/**
 * Service interface for managing notifications.
 */
public interface NotificationService {

    /**
     * Creates and persists a new notification.
     *
     * @param branchId the ID of the organization branch
     * @param recipientId the ID of the recipient employee (optional, can be null)
     * @param senderId the ID of the sender employee
     * @param title the title of the notification
     * @param content the content body of the notification
     * @param type the type of the notification
     * @return the saved Notification entity
     */
    Notification create(
            String branchId,
            String recipientId,
            String senderId,
            String title,
            String content,
            NotificationType type
    );

    /**
     * Retrieves a page of notifications for a branch and employee context.
     *
     * @param branchId the ID of the branch
     * @param employeeId the ID of the employee
     * @param page the page number (1-indexed)
     * @param size the page size
     * @return a PagingResponse of NotificationResponse DTOs
     */
    PagingResponse<NotificationResponse> getNotifications(
            String branchId,
            String employeeId,
            int page,
            int size
    );
}
