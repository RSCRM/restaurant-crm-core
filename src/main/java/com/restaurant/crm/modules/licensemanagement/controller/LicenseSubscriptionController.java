package com.restaurant.crm.modules.licensemanagement.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.SubscriptionSearchRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseSubscriptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseSubscriptionController {

    LicenseSubscriptionService licenseSubscriptionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> grantSubscription(
            @Valid @RequestBody GrantSubscriptionRequest request
    ) {
        SubscriptionResponse subscriptionResponse = licenseSubscriptionService.grantSubscription(request);

        ApiResponse<SubscriptionResponse> response = ApiResponse.<SubscriptionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(subscriptionResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/{id}/renew")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> renewSubscription(
            @PathVariable String id
    ) {
        SubscriptionResponse subscriptionResponse = licenseSubscriptionService.renewSubscription(id);

        ApiResponse<SubscriptionResponse> response = ApiResponse.<SubscriptionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(subscriptionResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/{id}/revoke")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> revokeSubscription(
            @PathVariable String id
    ) {
        SubscriptionResponse subscriptionResponse = licenseSubscriptionService.revokeSubscription(id);

        ApiResponse<SubscriptionResponse> response = ApiResponse.<SubscriptionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(subscriptionResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PostMapping("/organization/{organizationId}/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<PagingResponse<SubscriptionResponse>>> searchSubscriptionsByOrganization(
            @PathVariable String organizationId,
            @RequestBody SubscriptionSearchRequest searchRequest,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field
    ) {
        PagingRequest pagingRequest = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        ApiResponse<PagingResponse<SubscriptionResponse>> response = ApiResponse.<PagingResponse<SubscriptionResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseSubscriptionService.searchSubscriptionsByOrganization(organizationId, searchRequest, pagingRequest))
                .build();

        return ResponseEntity.ok(response);
    }
}
