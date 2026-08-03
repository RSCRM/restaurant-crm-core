package com.restaurant.crm.modules.erp.organization.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.OrgRoleResponse;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Comparator;
import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/org-roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrgRoleController {

    OrgRoleRepository orgRoleRepository;

    @GetMapping
    @PreAuthorize("hasAuthority('STAFF_VIEW') or hasAuthority('STAFF_MANAGE') or hasAuthority('EMPLOYEE_ADD') or hasAuthority('EMPLOYEE_UPDATE') or hasAuthority('BRANCH_MANAGER_ASSIGN')")
    public ResponseEntity<ApiResponse<List<OrgRoleResponse>>> getOrgRoles() {
        List<OrgRoleResponse> data = orgRoleRepository.findAll()
                .stream()
                .map(role -> OrgRoleResponse.builder()
                        .id(role.getId())
                        .roleName(role.getRoleName())
                        .dataScope(role.getDataScope())
                        .build())
                .sorted(Comparator.comparing(OrgRoleResponse::getRoleName))
                .toList();

        return ResponseEntity.ok(ApiResponse.<List<OrgRoleResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(data)
                .build());
    }
}
