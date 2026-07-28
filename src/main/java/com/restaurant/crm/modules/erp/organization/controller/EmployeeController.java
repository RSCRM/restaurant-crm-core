package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgRole;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
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

    @GetMapping("/{branchId}/manager")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_VIEW + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeBranchAssignmentResponse>> getBranchManager(
            @PathVariable String branchId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeBranchAssignmentResponse>builder()
                        .data(employeeService.getBranchManager(branchId))
                        .build()
        );
    }

    @PutMapping("/{branchId}/manager")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_UPDATE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeBranchAssignmentResponse>> assignBranchManager(
            @PathVariable String branchId,
            @Valid @RequestBody EmployeeBranchAssignmentRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeBranchAssignmentResponse>builder()
                        .data(employeeService.assignToBranch(branchId, request))
                        .build()
        );
    }

    @DeleteMapping("/{branchId}/manager")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.BRANCH_MANAGER_DELETE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeBranchAssignmentResponse>> removeBranchManager(
            @PathVariable String branchId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeBranchAssignmentResponse>builder()
                        .data(employeeService.removeManager(branchId))
                        .build()
        );
    }
}
