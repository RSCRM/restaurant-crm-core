package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/personal/branches")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeController {

    EmployeeService employeeService;

    @PutMapping("/{branchId}/manager")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.BRANCH_MANAGER_ASSIGN + "')")
    public ResponseEntity<ApiResponse<EmployeeBranchAssignmentResponse>> assignManagerToBranch(
            @PathVariable String branchId,
            @Valid @RequestBody EmployeeBranchAssignmentRequest request
    ) {
        ApiResponse<EmployeeBranchAssignmentResponse> response = ApiResponse.<EmployeeBranchAssignmentResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.assignToBranch(branchId, request))
                .build();

        return ResponseEntity.ok(response);
    }
}
