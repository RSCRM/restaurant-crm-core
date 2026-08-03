package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.OrganizationSearchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/api/v1/erp/organizations")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationController {

    OrganizationService organizationService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORGANIZATION_MANAGE')")
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORGANIZATION_VIEW')")
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

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagingResponse<OrganizationResponse>>> searchOrganizations(
            @RequestBody OrganizationSearchRequest searchRequest,
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

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<OrganizationResponse>>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(organizationService.searchOrganizations(searchRequest, pagingRequest))
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORGANIZATION_VIEW')")
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORGANIZATION_VIEW')")
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
    @PreAuthorize("hasRole('ADMIN') or hasAuthority('ORGANIZATION_MANAGE')")
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