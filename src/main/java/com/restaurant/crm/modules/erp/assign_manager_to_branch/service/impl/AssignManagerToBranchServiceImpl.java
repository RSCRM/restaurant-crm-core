package com.restaurant.crm.modules.erp.assign_manager_to_branch.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.assign_manager_to_branch.dto.request.BranchManagerAssignmentRequest;
import com.restaurant.crm.modules.erp.assign_manager_to_branch.service.interfaces.AssignManagerToBranchService;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.manage_branch_manager.mapper.BranchManagerMapper;
import com.restaurant.crm.modules.erp.shared.constants.org_permission.PredefinedOrgPermission;
import com.restaurant.crm.modules.erp.shared.constants.org_role.PredefinedOrgRole;
import com.restaurant.crm.modules.erp.shared.entity.Employee;
import com.restaurant.crm.modules.erp.shared.entity.OrgPermission;
import com.restaurant.crm.modules.erp.shared.entity.OrgRole;
import com.restaurant.crm.modules.erp.shared.entity.Organization;
import com.restaurant.crm.modules.erp.shared.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.shared.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.shared.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrganizationBranchRepository;
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
public class AssignManagerToBranchServiceImpl implements AssignManagerToBranchService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    OrgRoleRepository orgRoleRepository;
    OrgPermissionRepository orgPermissionRepository;
    BranchManagerMapper branchManagerMapper;

    @Override
    @Transactional
    public BranchManagerResponse assignToBranch(String branchId, BranchManagerAssignmentRequest request) {
        String ownerId = getCurrentOwnerId();
        OrganizationBranch targetBranch = findBranch(branchId, ownerId);
        Employee branchManager = findBranchManager(request.getBranchManagerId(), ownerId);

        if (!EmployeeStatus.ACTIVE.equals(branchManager.getStatus()) || !branchManager.getUser().isEnabled()) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }

        if (branchManager.getId().equals(targetBranch.getManagerId())) {
            return branchManagerMapper.toBranchManagerResponse(branchManager);
        }

        ensureBranchHasNoOtherActiveManager(targetBranch.getId(), branchManager.getId());
        clearCurrentBranchAssignment(branchManager);

        branchManager.setBranch(targetBranch);
        branchManager.setOrgRole(getOrCreateBranchManagerRole(targetBranch.getOrganization()));
        targetBranch.setManagerId(branchManager.getId());
        branchRepository.save(targetBranch);

        Employee assigned = employeeRepository.save(branchManager);
        return branchManagerMapper.toBranchManagerResponse(assigned);
    }

    private Employee findBranchManager(String branchManagerId, String ownerId) {
        return employeeRepository.findByIdAndOrgRole_RoleNameAndBranch_Organization_OwnerId(
                        branchManagerId,
                        PredefinedOrgRole.BRANCH_MANAGER,
                        ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOrganization_OwnerId(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private OrgRole getOrCreateBranchManagerRole(Organization organization) {
        return orgRoleRepository.findByOrganization_IdAndRoleName(
                        organization.getId(),
                        PredefinedOrgRole.BRANCH_MANAGER)
                .orElseGet(() -> orgRoleRepository.save(OrgRole.builder()
                        .organization(organization)
                        .roleName(PredefinedOrgRole.BRANCH_MANAGER)
                        .description("Branch manager")
                        .permissions(getDefaultBranchManagerPermissions())
                        .build()));
    }

    private Set<OrgPermission> getDefaultBranchManagerPermissions() {
        Set<OrgPermission> permissions = new HashSet<>();
        Set.of(
                PredefinedOrgPermission.MANAGE_TABLE,
                PredefinedOrgPermission.MANAGE_MENU,
                PredefinedOrgPermission.MANAGE_EMPLOYEE,
                PredefinedOrgPermission.MANAGE_INVENTORY,
                PredefinedOrgPermission.VIEW_BRANCH_REPORT
        ).forEach(permissionCode -> orgPermissionRepository.findByPermissionCode(permissionCode)
                .ifPresent(permissions::add));
        return permissions;
    }

    private void ensureBranchHasNoOtherActiveManager(String branchId, String currentEmployeeId) {
        boolean existed = employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatusAndIdNot(
                branchId,
                PredefinedOrgRole.BRANCH_MANAGER,
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
