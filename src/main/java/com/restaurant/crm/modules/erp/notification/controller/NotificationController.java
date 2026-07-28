package com.restaurant.crm.modules.erp.notification.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.notification.dto.response.NotificationResponse;
import com.restaurant.crm.common.notification.dto.response.UnreadCountResponse;
import com.restaurant.crm.common.notification.security.RecipientContext;
import com.restaurant.crm.common.notification.service.interfaces.NotificationQueryService;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.notification.constants.NotificationControllerConstants;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Staff-facing notification endpoints.
 * <p>
 * No endpoint here accepts an organization or branch identifier: the tenant is derived from the
 * access token via {@link RecipientContext}. Accepting it from the query string let any
 * authenticated employee stream and read another organization's notifications.
 * <p>
 * None of them carries a business permission either. Every employee needs a notification channel;
 * filtering happens per notification through {@code requiredPermission}, not per endpoint.
 */
@RestController
@RequestMapping(NotificationControllerConstants.BASE_PATH)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationQueryService notificationQueryService;
    SseEmitterService sseEmitterService;

    /**
     * Opens the real-time stream for the caller's own branch.
     *
     * @return the SSE connection
     */
    @GetMapping(value = NotificationControllerConstants.PATH_SUBSCRIBE,
                produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("isAuthenticated()")
    public SseEmitter subscribe() {
        RecipientContext context = RecipientContext.fromSecurityContext();
        return sseEmitterService.createEmitter(context.requireBranchId());
    }

    /**
     * Returns the caller's notification feed: system-wide, branch, matching group and personal
     * notifications, newest first.
     *
     * @param page 1-indexed page number
     * @param size page size
     */
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<PagingResponse<NotificationResponse>>> getFeed(
            @RequestParam(value = NotificationControllerConstants.PARAM_PAGE,
                          required = false,
                          defaultValue = NotificationControllerConstants.DEFAULT_PAGE) int page,
            @RequestParam(value = NotificationControllerConstants.PARAM_SIZE,
                          required = false,
                          defaultValue = NotificationControllerConstants.DEFAULT_SIZE) int size
    ) {
        PagingResponse<NotificationResponse> feed = notificationQueryService.getFeed(
                RecipientContext.fromSecurityContext(), page, size);

        return ResponseEntity.ok(ApiResponse.<PagingResponse<NotificationResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(feed)
                .build());
    }

    /**
     * Returns how many visible notifications the caller has not acknowledged yet.
     */
    @GetMapping(NotificationControllerConstants.PATH_UNREAD_COUNT)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UnreadCountResponse>> getUnreadCount() {
        long unread = notificationQueryService.countUnread(RecipientContext.fromSecurityContext());

        return ResponseEntity.ok(ApiResponse.<UnreadCountResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(UnreadCountResponse.builder().unreadCount(unread).build())
                .build());
    }

    /**
     * Acknowledges one notification for the calling employee only.
     */
    @PutMapping(NotificationControllerConstants.PATH_MARK_READ)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Void>> markRead(@PathVariable String notificationId) {
        notificationQueryService.markRead(RecipientContext.fromSecurityContext(), notificationId);

        return ResponseEntity.ok(ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .build());
    }

    /**
     * Acknowledges the caller's visible unread notifications.
     *
     * @return how many were acknowledged
     */
    @PutMapping(NotificationControllerConstants.PATH_MARK_ALL_READ)
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<Integer>> markAllRead() {
        int acknowledged = notificationQueryService.markAllRead(RecipientContext.fromSecurityContext());

        return ResponseEntity.ok(ApiResponse.<Integer>builder()
                .success(ApiConstant.SUCCESS)
                .data(acknowledged)
                .build());
    }
}