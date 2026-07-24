package com.restaurant.crm.modules.erp.notification.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.notification.dto.response.NotificationResponse;
import com.restaurant.crm.modules.erp.notification.service.interfaces.NotificationService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Controller exposing SSE and REST endpoints for receiving and querying notifications.
 */
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class NotificationController {

    NotificationService notificationService;
    SseEmitterService sseEmitterService;

    /**
     * Subscribes to receive real-time server-sent events for a specific branch.
     * Produces a text/event-stream response.
     * Accessible by employees with ORDER_READ permission.
     *
     * @param branchId the ID of the branch to subscribe to
     * @return the SseEmitter representing the event stream connection
     */
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_READ)")
    public SseEmitter subscribe(@RequestParam String branchId) {
        return sseEmitterService.createEmitter(branchId);
    }

    /**
     * Retrieves the notification history for a branch.
     * Filters notifications directed to the current logged-in employee or broadcasted to the branch.
     * Accessible by employees with ORDER_READ permission.
     *
     * @param branchId the branch ID
     * @param page the page number (default 1)
     * @param size the page size (default 10)
     * @return the paginated list of notifications
     */
    @GetMapping
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_READ)")
    public ResponseEntity<ApiResponse<PagingResponse<NotificationResponse>>> getHistory(
            @RequestParam String branchId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        // Resolve current employee ID from authentication context
        String employeeId = null;
        try {
            employeeId = AuthUtils.getEmployeeId();
        } catch (Exception e) {
            // Ignore if called from an unauthenticated test environment
        }

        PagingResponse<NotificationResponse> history = notificationService.getNotifications(
                branchId,
                employeeId,
                page,
                size
        );

        ApiResponse<PagingResponse<NotificationResponse>> apiResponse = ApiResponse.<PagingResponse<NotificationResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(history)
                .build();

        return ResponseEntity.ok(apiResponse);
    }
}
