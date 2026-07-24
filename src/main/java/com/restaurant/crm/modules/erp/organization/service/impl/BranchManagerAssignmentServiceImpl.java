package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.BranchManagerAssignmentConstants;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.BranchManagerAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.BranchManagerAssignmentMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.BranchManagerAssignmentService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchManagerAssignmentServiceImpl implements BranchManagerAssignmentService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    OrgRoleRepository orgRoleRepository;
    OrgPermissionRepository orgPermissionRepository;
    BranchManagerAssignmentMapper branchManagerAssignmentMapper;

    @Override
    @Transactional
    public BranchManagerAssignmentResponse assignToBranch(String branchId, BranchManagerAssignmentRequest request) {
        String ownerId = getCurrentOwnerId();
        OrganizationBranch targetBranch = findBranch(branchId, ownerId);
        Employee branchManager = findBranchManager(request.getBranchManagerId(), ownerId);

        if (!EmployeeStatus.ACTIVE.equals(branchManager.getStatus()) || !branchManager.getUser().isEnabled()) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }

        if (branchManager.getId().equals(targetBranch.getManagerId())) {
            return branchManagerAssignmentMapper.toBranchManagerAssignmentResponse(branchManager);
        }

        ensureBranchHasNoOtherActiveManager(targetBranch.getId(), branchManager.getId());
        clearCurrentBranchAssignment(branchManager);

        branchManager.setBranch(targetBranch);
        branchManager.setOrgRole(getOrCreateBranchManagerRole());
        targetBranch.setManagerId(branchManager.getId());
        branchRepository.save(targetBranch);

        Employee assigned = employeeRepository.save(branchManager);
        return branchManagerAssignmentMapper.toBranchManagerAssignmentResponse(assigned);
    }

    private Employee findBranchManager(String branchManagerId, String ownerId) {
        return employeeRepository.findByIdAndOrgRole_RoleNameAndBranch_Organization_OwnerId(
                        branchManagerId,
                        BranchManagerAssignmentConstants.BRANCH_MANAGER_ROLE,
                        ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOrganization_OwnerId(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private OrgRole getOrCreateBranchManagerRole() {
        return orgRoleRepository.findByRoleName(BranchManagerAssignmentConstants.BRANCH_MANAGER_ROLE)
                .orElseGet(() -> orgRoleRepository.save(OrgRole.builder()
                        .roleName(BranchManagerAssignmentConstants.BRANCH_MANAGER_ROLE)
                        .orgPermissions(getDefaultBranchManagerPermissions())
                        .build()));
    }

    private Set<OrgPermission> getDefaultBranchManagerPermissions() {
        Set<OrgPermission> permissions = new HashSet<>();
        Set.of(
                StartDefinedOrgPermission.TABLE_MANAGE,
                StartDefinedOrgPermission.MENU_MANAGE,
                StartDefinedOrgPermission.STAFF_MANAGE,
                StartDefinedOrgPermission.REPORT_VIEW
        ).forEach(permissionName -> orgPermissionRepository.findByPermissionName(permissionName)
                .ifPresent(permissions::add));
        return permissions;
    }

    private void ensureBranchHasNoOtherActiveManager(String branchId, String currentEmployeeId) {
        boolean existed = employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatusAndIdNot(
                branchId,
                BranchManagerAssignmentConstants.BRANCH_MANAGER_ROLE,
                EmployeeStatus.ACTIVE,
                currentEmployeeId
        );

        if (existed) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_ALREADY_ASSIGNED);
        }
    }

    private void clearCurrentBranchAssignment(Employee employee) {
        OrganizationBranch currentBranch = employee.getBranch();
        if (employee.getId().equals(currentBranch.getManagerId())) {
            currentBranch.setManagerId(null);
            branchRepository.save(currentBranch);
        }
    }

    private String getCurrentOwnerId() {
        String ownerId = AuthUtils.getCurrentUserId();
        if (!StringUtils.hasText(ownerId)) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
        return ownerId;
    }
}
