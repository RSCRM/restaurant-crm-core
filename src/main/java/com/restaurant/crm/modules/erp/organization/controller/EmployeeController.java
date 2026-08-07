package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    @GetMapping("/employees")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_VIEW + "')")
    public ResponseEntity<ApiResponse<List<EmployeeResponse>>> listEmployees() {
        return ResponseEntity.ok(ApiResponse.<List<EmployeeResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.listEmployees())
                .build());
    }

    @PostMapping("/employees")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_ADD + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> addEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        EmployeeResponse data = employeeService.addEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(
                ApiResponse.<EmployeeResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(data)
                        .build()
        );
    }

    @PutMapping("/employees/{id}/role")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_ROLE_ASSIGN + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> assignRole(
            @PathVariable String id,
            @Valid @RequestBody AssignRoleRequest request
    ) {
        EmployeeResponse data = employeeService.assignRole(id, request);
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(data)
                        .build()
        );
    }

    @DeleteMapping("/employees/{id}/role")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_ROLE_REVOKE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> revokeRole(
            @PathVariable String id
    ) {
        EmployeeResponse data = employeeService.revokeRole(id);
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(data)
                        .build()
        );
    }

    @PutMapping("/employees/{id}/salary")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> configSalary(
            @PathVariable String id,
            @Valid @RequestBody SalaryConfigRequest request
    ) {
        EmployeeResponse data = employeeService.configSalary(id, request);
        return ResponseEntity.ok(
                ApiResponse.<EmployeeResponse>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(data)
                        .build()
        );
    }

    @PutMapping("/employees/{id}")
    @PreAuthorize("hasAuthority('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.updateEmployee(id, request))
                .build());
    }
}
