package com.restaurant.crm.modules.crm.pointwallet.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import org.springframework.web.bind.annotation.PathVariable;

@RestController
@RequestMapping("/api/v1/crm/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointWalletController {

    PointWalletService pointWalletService;

    @GetMapping("/balance")
    @PreAuthorize("hasAuthority('POINT_WALLET_READ')")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> getBalance(
            @RequestParam String customerId,
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String restaurantId
    ) {
        String targetOrgId = organizationId != null ? organizationId : restaurantId;
        CustomerPointResponse response = pointWalletService.getWallet(customerId, targetOrgId);
        return ResponseEntity.ok(ApiResponse.<CustomerPointResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/history")
    @PreAuthorize("hasAuthority('POINT_WALLET_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<CustomerPointHistoryResponse>>> getHistory(
            @RequestParam String customerId,
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String restaurantId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        String targetOrgId = organizationId != null ? organizationId : restaurantId;
        PagingResponse<CustomerPointHistoryResponse> response = pointWalletService.getHistory(customerId, targetOrgId, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<CustomerPointHistoryResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/organization/{organizationId}/list")
    @PreAuthorize("hasAuthority('POINT_WALLET_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<CustomerPointResponse>>> getOrganizationCustomers(
            @PathVariable String organizationId,
            @RequestParam(value = "searchPhone", required = false) String searchPhone,
            @RequestParam(value = "minPoints", required = false) Integer minPoints,
            @RequestParam(value = "maxPoints", required = false) Integer maxPoints,
            @RequestParam(value = "minLifetimePoints", required = false) Integer minLifetimePoints,
            @RequestParam(value = "maxLifetimePoints", required = false) Integer maxLifetimePoints,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(value = "sortBy", required = false, defaultValue = "updatedAt") String sortBy,
            @RequestParam(value = "sortDirection", required = false, defaultValue = "DESC") String sortDirection
    ) {
        PagingResponse<CustomerPointResponse> response = pointWalletService.getOrganizationCustomers(
                organizationId, searchPhone, minPoints, maxPoints, minLifetimePoints, maxLifetimePoints,
                page, size, sortBy, sortDirection
        );
        return ResponseEntity.ok(ApiResponse.<PagingResponse<CustomerPointResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}
