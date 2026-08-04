package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
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
    @PreAuthorize("@employeeAccessChecker.canManage('STAFF_VIEW') or @employeeAccessChecker.canManage('STAFF_MANAGE')")
    public ResponseEntity<ApiResponse<PagingResponse<EmployeeResponse>>> getEmployees(
            @RequestParam(required = false) String organizationId,
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role,
            @RequestParam(required = false) String status,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size,
            @RequestParam(required = false) String field,
            @RequestParam(required = false) String direction
    ) {
        PagingResponse<EmployeeResponse> data = employeeService.getEmployees(
                organizationId,
                branchId,
                keyword,
                role,
                status,
                page,
                size,
                field,
                direction
        );

        return ResponseEntity.ok(ApiResponse.<PagingResponse<EmployeeResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(data)
                .build());
    }

    @GetMapping("/{id}")
    @PreAuthorize("@employeeAccessChecker.canManage('STAFF_VIEW') or @employeeAccessChecker.canManage('STAFF_MANAGE')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> getEmployee(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.getEmployee(id))
                .build());
    }

    @PostMapping
    @PreAuthorize("@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_ADD + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> createEmployee(
            @Valid @RequestBody CreateEmployeeRequest request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.addEmployee(request))
                .build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> updateEmployee(
            @PathVariable String id,
            @Valid @RequestBody UpdateEmployeeRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.updateEmployee(id, request))
                .build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_DELETE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> deleteEmployee(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.deleteEmployee(id))
                .build());
    }

    @PatchMapping("/{id}/enable")
    @PreAuthorize("@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> enableEmployee(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.enableEmployee(id))
                .build());
    }

    @PatchMapping("/{id}/disable")
    @PreAuthorize("@employeeAccessChecker.canManage('" + EmployeeConstants.EMPLOYEE_UPDATE + "')")
    public ResponseEntity<ApiResponse<EmployeeResponse>> disableEmployee(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<EmployeeResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(employeeService.disableEmployee(id))
                .build());
    }
}
