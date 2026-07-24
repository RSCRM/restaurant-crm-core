package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationBranchService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/organization-branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationBranchController {

    OrganizationBranchService organizationBranchService;

    @PostMapping
    @PreAuthorize("hasAuthority('ORGANIZATION_BRANCH_MANAGE')")
    public ResponseEntity<ApiResponse<OrganizationBranchResponse>> createOrganizationBranch(
            @Valid @RequestBody CreateOrganizationBranchRequest request
    ) {

        OrganizationBranchResponse response =
                organizationBranchService.createOrganizationBranch(request);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationBranchResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/organization/{organizationId}")
    @PreAuthorize("hasAuthority('ORGANIZATION_BRANCH_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<OrganizationBranchResponse>>> getOrganizationBranches(
            @PathVariable String organizationId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<OrganizationBranchResponse> response =
                organizationBranchService.getOrganizationBranches(
                        organizationId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<OrganizationBranchResponse>>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_BRANCH_VIEW')")
    public ResponseEntity<ApiResponse<OrganizationBranchResponse>> getOrganizationBranchById(
            @PathVariable String id
    ) {

        OrganizationBranchResponse response =
                organizationBranchService.getOrganizationBranchById(id);

        return ResponseEntity.ok(
                ApiResponse.<OrganizationBranchResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('ORGANIZATION_BRANCH_MANAGE')")
    public ResponseEntity<ApiResponse<OrganizationBranchResponse>> updateOrganizationBranch(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrganizationBranchRequest request
    ) {

        OrganizationBranchResponse response =
                organizationBranchService.updateOrganizationBranch(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<OrganizationBranchResponse>builder()
                        .data(response)
                        .build()
        );
    }
}