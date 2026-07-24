package com.restaurant.crm.modules.licensemanagement.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseSubscriptionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/subscriptions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseSubscriptionController {

    LicenseSubscriptionService licenseSubscriptionService;

    @PostMapping
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
}
