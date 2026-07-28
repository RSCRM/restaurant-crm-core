package com.restaurant.crm.common.notification.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.notification.dto.response.NotificationResponse;
import com.restaurant.crm.common.notification.security.RecipientContext;

/**
 * Read side of the shared notification infrastructure.
 * <p>
 * Every method takes the caller's {@link RecipientContext} and applies the visibility predicate;
 * none of them accepts a tenant identifier from the request.
 */
public interface NotificationQueryService {

    /**
     * @param page 1-indexed, matching the project-wide pagination convention
     */
    PagingResponse<NotificationResponse> getFeed(RecipientContext context, int page, int size);

    long countUnread(RecipientContext context);

    /**
     * Acknowledges one notification.
     *
     * @throws com.restaurant.crm.common.exception.AppException 404 if the notification does not exist
     *                                                          <em>or</em> is not visible to the caller
     */
    void markRead(RecipientContext context, String notificationId);

    /**
     * Acknowledges the caller's visible unread notifications, newest first.
     *
     * @return how many were acknowledged, capped by {@code MARK_ALL_READ_LIMIT}
     */
    int markAllRead(RecipientContext context);
}