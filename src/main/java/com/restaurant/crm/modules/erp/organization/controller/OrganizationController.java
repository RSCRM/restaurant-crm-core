package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/organizations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationController {

    OrganizationService organizationService;

    @PostMapping
    public ResponseEntity<ApiResponse<OrganizationResponse>> createOrganization(
            @Valid @RequestBody CreateOrganizationRequest request
    ) {

        OrganizationResponse response =
                organizationService.createOrganization(request);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PagingResponse<OrganizationResponse>>> getOrganizations(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<OrganizationResponse> response =
                organizationService.getOrganizations(page, size);

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<OrganizationResponse>>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationById(
            @PathVariable String id
    ) {

        OrganizationResponse response =
                organizationService.getOrganizationById(id);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> getOrganizationByOwnerId(
            @PathVariable String ownerId
    ) {

        OrganizationResponse response =
                organizationService.getOrganizationByOwnerId(ownerId);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<OrganizationResponse>> updateOrganization(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationRequest request
    ) {

        OrganizationResponse response =
                organizationService.updateOrganization(id, request);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationResponse>builder()
                        .data(response)
                        .build()
        );
    }
}
