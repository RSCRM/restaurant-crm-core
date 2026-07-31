package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.constants.CustomerOrderTrackingControllerConstants;
import com.restaurant.crm.modules.erp.order.dto.response.CustomerOrderTrackingResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerOrderTrackingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

/**
 * Customer order-tracking endpoints (uc-c-06). Requires a CUSTOMER_SESSION token; the order id is
 * resolved from the session, never from the client. Real time reuses the order-keyed SSE channel.
 */
@Tag(name = "Customer Order Tracking", description = "uc-c-06 — track cooking progress in real time")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('" + CustomerOrderTrackingControllerConstants.ROLE_CUSTOMER_SESSION + "')")
public class CustomerOrderTrackingController {

    CustomerOrderTrackingService customerOrderTrackingService;

    @Operation(summary = "Get the current order's cooking progress for this session")
    @GetMapping(CustomerOrderTrackingControllerConstants.BASE
            + CustomerOrderTrackingControllerConstants.PATH_COOKING_STATUS)
    public ResponseEntity<ApiResponse<CustomerOrderTrackingResponse>> getCookingStatus() {
        return ResponseEntity.ok(ApiResponse.<CustomerOrderTrackingResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(customerOrderTrackingService.getCurrentCookingStatus())
                .build());
    }

    @Operation(summary = "Subscribe (SSE) to cooking-status updates for this session's order")
    @GetMapping(value = CustomerOrderTrackingControllerConstants.BASE
            + CustomerOrderTrackingControllerConstants.PATH_SUBSCRIBE,
            produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribe() {
        return customerOrderTrackingService.subscribeToCookingStatus();
    }
}
