package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.constants.QrSessionControllerConstants;
import com.restaurant.crm.modules.erp.order.dto.request.QrResolveRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionJoinRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrResolveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.QrSessionResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.QrSessionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;


@Tag(name = "QR Table Ordering", description = "scan QR, open/join a group ordering session")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QrSessionController {

    QrSessionService qrSessionService;

    @Operation(summary = "Resolve a scanned TABLE QR (public, read-only)")
    @PostMapping(QrSessionControllerConstants.BASE_PUBLIC + QrSessionControllerConstants.PATH_RESOLVE)
    public ResponseEntity<ApiResponse<QrResolveResponse>> resolve(
            @Valid @RequestBody QrResolveRequest request) {
        return ResponseEntity.ok(ApiResponse.<QrResolveResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.resolve(request))
                .build());
    }

    @Operation(summary = "Open a session as OWNER after OTP (public)")
    @PostMapping(QrSessionControllerConstants.BASE_PUBLIC + QrSessionControllerConstants.PATH_SESSION)
    public ResponseEntity<ApiResponse<QrSessionResponse>> start(
            @Valid @RequestBody QrSessionStartRequest request) {
        return ResponseEntity.ok(ApiResponse.<QrSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.start(request))
                .build());
    }

    @Operation(summary = "Join a session as MEMBER via GROUP QR (public)")
    @PostMapping(QrSessionControllerConstants.BASE_PUBLIC + QrSessionControllerConstants.PATH_SESSION_JOIN)
    public ResponseEntity<ApiResponse<QrSessionResponse>> join(
            @Valid @RequestBody QrSessionJoinRequest request) {
        return ResponseEntity.ok(ApiResponse.<QrSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.join(request))
                .build());
    }

    @Operation(summary = "Read the current session (CUSTOMER_SESSION token)")
    @GetMapping(QrSessionControllerConstants.BASE_SESSION + QrSessionControllerConstants.PATH_SESSION)
    @PreAuthorize("hasRole('" + QrSessionControllerConstants.ROLE_CUSTOMER_SESSION + "')")
    public ResponseEntity<ApiResponse<QrSessionResponse>> getCurrent() {
        return ResponseEntity.ok(ApiResponse.<QrSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.getCurrent())
                .build());
    }

    @Operation(summary = "OWNER: issue a fresh GROUP QR (CUSTOMER_SESSION token)")
    @PostMapping(QrSessionControllerConstants.BASE_SESSION
            + QrSessionControllerConstants.PATH_SESSION_REFRESH_GROUP_QR)
    @PreAuthorize("hasRole('" + QrSessionControllerConstants.ROLE_CUSTOMER_SESSION + "')")
    public ResponseEntity<ApiResponse<QrSessionResponse>> refreshGroupQr() {
        return ResponseEntity.ok(ApiResponse.<QrSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.refreshGroupQr())
                .build());
    }

    @Operation(summary = "Keep the session alive (CUSTOMER_SESSION token)")
    @PostMapping(QrSessionControllerConstants.BASE_SESSION
            + QrSessionControllerConstants.PATH_SESSION_HEARTBEAT)
    @PreAuthorize("hasRole('" + QrSessionControllerConstants.ROLE_CUSTOMER_SESSION + "')")
    public ResponseEntity<ApiResponse<QrSessionResponse>> heartbeat() {
        return ResponseEntity.ok(ApiResponse.<QrSessionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(qrSessionService.heartbeat())
                .build());
    }
}
