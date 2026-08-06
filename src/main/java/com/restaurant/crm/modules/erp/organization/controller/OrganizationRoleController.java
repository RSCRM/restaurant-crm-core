package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrgRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgPermissionResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgRoleResponse;
import com.restaurant.crm.modules.erp.organization.service.interfaces.OrganizationRoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrganizationRoleController {

    OrganizationRoleService service;

    @PostMapping("/org-roles")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.ORG_ROLE_MANAGE + "')")
    public ResponseEntity<ApiResponse<OrgRoleResponse>> create(@Valid @RequestBody CreateOrgRoleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<OrgRoleResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.create(request)).build());
    }

    @PutMapping("/org-roles/{id}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.ORG_ROLE_MANAGE + "')")
    public ResponseEntity<ApiResponse<OrgRoleResponse>> update(@PathVariable String id, @Valid @RequestBody UpdateOrgRoleRequest request) {
        return ResponseEntity.ok(ApiResponse.<OrgRoleResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.update(id, request)).build());
    }

    @GetMapping("/org-roles")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.ORG_ROLE_MANAGE + "') "
            + "or hasAuthority('" + EmployeeConstants.EMPLOYEE_ROLE_ASSIGN + "')")
    public ResponseEntity<ApiResponse<List<OrgRoleResponse>>> list() {
        return ResponseEntity.ok(ApiResponse.<List<OrgRoleResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listOrgRoles()).build());
    }

    @GetMapping("/org-roles/{id}")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.ORG_ROLE_MANAGE + "') "
            + "or hasAuthority('" + EmployeeConstants.EMPLOYEE_ROLE_ASSIGN + "')")
    public ResponseEntity<ApiResponse<OrgRoleResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<OrgRoleResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.get(id)).build());
    }

    @GetMapping("/org-permissions")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.ORG_ROLE_MANAGE + "')")
    public ResponseEntity<ApiResponse<List<OrgPermissionResponse>>> listPermissions() {
        return ResponseEntity.ok(ApiResponse.<List<OrgPermissionResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listPermissions()).build());
    }
}
