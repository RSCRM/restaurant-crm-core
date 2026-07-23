package com.restaurant.crm.modules.licensemanagement.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.UpdateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.DeleteLicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;
import com.restaurant.crm.modules.licensemanagement.service.interfaces.LicenseService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/admin/licenses")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class LicenseController {

    LicenseService licenseService;

    @PostMapping
    public ResponseEntity<ApiResponse<LicenseResponse>> createLicense(
            @Valid @RequestBody CreateLicenseRequest request
    ) {
        LicenseResponse licenseResponse = licenseService.createLicense(request);

        ApiResponse<LicenseResponse> response = ApiResponse.<LicenseResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseResponse)
                .build();

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagingResponse<LicenseResponse>>> getLicenses(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field
    ) {
        PagingRequest request = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        ApiResponse<PagingResponse<LicenseResponse>> response = ApiResponse.<PagingResponse<LicenseResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseService.getLicenses(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<LicenseResponse>> updateLicense(
            @PathVariable String id,
            @Valid @RequestBody UpdateLicenseRequest request
    ) {
        LicenseResponse licenseResponse = licenseService.updateLicense(id, request);

        ApiResponse<LicenseResponse> response = ApiResponse.<LicenseResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<DeleteLicenseResponse>> deleteLicense(
            @PathVariable String id
    ) {
        DeleteLicenseResponse deleteResponse = licenseService.deleteLicense(id);

        ApiResponse<DeleteLicenseResponse> response = ApiResponse.<DeleteLicenseResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(deleteResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/lock")
    public ResponseEntity<ApiResponse<LicenseResponse>> lockLicense(
            @PathVariable String id
    ) {
        LicenseResponse licenseResponse = licenseService.lockLicense(id);

        ApiResponse<LicenseResponse> response = ApiResponse.<LicenseResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseResponse)
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{id}/reactivate")
    public ResponseEntity<ApiResponse<LicenseResponse>> reactivateLicense(
            @PathVariable String id
    ) {
        LicenseResponse licenseResponse = licenseService.reactivateLicense(id);

        ApiResponse<LicenseResponse> response = ApiResponse.<LicenseResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(licenseResponse)
                .build();

        return ResponseEntity.ok(response);
    }
}
