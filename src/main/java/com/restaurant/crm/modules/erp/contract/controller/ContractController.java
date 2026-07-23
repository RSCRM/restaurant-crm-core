package com.restaurant.crm.modules.erp.contract.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.contract.dto.response.LicenseInfoResponse;
import com.restaurant.crm.modules.erp.contract.service.interfaces.ContractService;
import com.restaurant.crm.modules.identity.constants.permission.StartDefinedPermission;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personal/contracts")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ContractController {

    ContractService contractService;

    @GetMapping("/license-info")
    @PreAuthorize("hasAuthority('" + StartDefinedPermission.CONTRACT_LICENSE_VIEW + "')")
    public ResponseEntity<ApiResponse<PagingResponse<LicenseInfoResponse>>> getMyLicenseInfos(
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

        ApiResponse<PagingResponse<LicenseInfoResponse>> response =
                ApiResponse.<PagingResponse<LicenseInfoResponse>>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(contractService.getMyLicenseInfos(request))
                        .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/organizations/{organizationId}/license-info")
    @PreAuthorize("hasAuthority('" + StartDefinedPermission.CONTRACT_LICENSE_VIEW + "')")
    public ResponseEntity<ApiResponse<LicenseInfoResponse>> getLicenseInfoByOrganization(
            @PathVariable String organizationId
    ) {
        ApiResponse<LicenseInfoResponse> response = ApiResponse.<LicenseInfoResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(contractService.getLicenseInfoByOrganization(organizationId))
                .build();

        return ResponseEntity.ok(response);
    }
}
