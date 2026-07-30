package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgRole;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
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
@RequestMapping("/api/v1/erp/employees")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeManagementController {

    EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('"
            + StartDefinedOrgPermission.STAFF_VIEW + "', '"
            + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<PagingResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(value = "organizationId", required = false) String organizationId,
            @RequestParam(value = "branchId", required = false) String branchId,
            @RequestParam(value = "keyword", required = false) String keyword,
            @RequestParam(value = "role", required = false) String role,
            @RequestParam(value = "status", required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(value = "field", defaultValue = "createdAt") String field,
            @RequestParam(value = "direction", defaultValue = "DESC") String direction
    ) {
        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<EmployeeResponse>>builder()
                        .data(employeeService.getEmployees(
                                organizationId,
                                branchId,
                                keyword,
                                role,
                                status,
                                page,
                                size,
                                field,
                                direction
                        ))
                        .build()
        );
    }

    @GetMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('"
            + StartDefinedOrgPermission.STAFF_VIEW + "', '"
            + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable String employeeId) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.getEmployee(employeeId))
                        .build()
        );
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.createEmployee(request))
                        .build()
        );
    }

    @PutMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String employeeId,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.updateEmployee(employeeId, request))
                        .build()
        );
    }

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> deleteEmployee(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.softDeleteEmployee(employeeId))
                        .build()
        );
    }

    @PatchMapping("/{employeeId}/enable")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> enableEmployee(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.enableEmployee(employeeId))
                        .build()
        );
    }

    @PatchMapping("/{employeeId}/disable")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.STAFF_MANAGE + "')"
            + " or hasAuthority('" + StartDefinedOrgRole.OWNER + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> disableEmployee(
            @PathVariable String employeeId
    ) {
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .data(employeeService.disableEmployee(employeeId))
                        .build()
        );
    }
}
