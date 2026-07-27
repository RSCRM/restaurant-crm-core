package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerCreationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerUpdateRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.BranchManagerService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personal/branch-managers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchManagerController {

    BranchManagerService branchManagerService;

    @PostMapping
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_CREATE + "')")
    public ResponseEntity<ApiResponse<BranchManagerResponse>> createBranchManager(
            @Valid @RequestBody BranchManagerCreationRequest request
    ) {
        ApiResponse<BranchManagerResponse> response = ApiResponse.<BranchManagerResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(branchManagerService.create(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_VIEW + "')")
    public ResponseEntity<ApiResponse<PagingResponse<BranchManagerResponse>>> getBranchManagers(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field,
            @RequestParam(required = false) String branchId
    ) {
        PagingRequest request = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        ApiResponse<PagingResponse<BranchManagerResponse>> response =
                ApiResponse.<PagingResponse<BranchManagerResponse>>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(branchManagerService.getBranchManagers(request, branchId))
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{branchManagerId}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_VIEW + "')")
    public ResponseEntity<ApiResponse<BranchManagerResponse>> getBranchManagerById(
            @PathVariable String branchManagerId
    ) {
        ApiResponse<BranchManagerResponse> response = ApiResponse.<BranchManagerResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(branchManagerService.getById(branchManagerId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{branchManagerId}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_UPDATE + "')")
    public ResponseEntity<ApiResponse<BranchManagerResponse>> updateBranchManager(
            @PathVariable String branchManagerId,
            @Valid @RequestBody BranchManagerUpdateRequest request
    ) {
        ApiResponse<BranchManagerResponse> response = ApiResponse.<BranchManagerResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(branchManagerService.update(branchManagerId, request))
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{branchManagerId}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteBranchManager(@PathVariable String branchManagerId) {
        branchManagerService.deleteById(branchManagerId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .build();

        return ResponseEntity.ok(response);
    }
}
