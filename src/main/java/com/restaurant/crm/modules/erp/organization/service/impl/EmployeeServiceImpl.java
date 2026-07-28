package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgRole;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    EmployeeMapper employeeMapper;

    @Override
    @Transactional(readOnly = true)
    public EmployeeBranchAssignmentResponse getBranchManager(String branchId) {
        OrganizationBranch branch = findOwnedBranch(branchId);

        return employeeMapper.toEmployeeBranchAssignmentResponse(branch);
    }

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse assignToBranch(
            String branchId,
            EmployeeBranchAssignmentRequest request
    ) {
        OrganizationBranch targetBranch = findOwnedBranch(branchId);
        String managerId = extractManagerId(request);
        Employee branchManager = employeeRepository.findByIdWithUserRoleAndBranch(managerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));

        validateAssignableManager(branchManager, targetBranch);

        if (hasSameManager(targetBranch, branchManager)) {
            return employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch);
        }

        clearManagerFromOtherBranch(branchManager.getId(), targetBranch.getId());
        targetBranch.setManager(branchManager);

        OrganizationBranch savedBranch = branchRepository.saveAndFlush(targetBranch);
        return employeeMapper.toEmployeeBranchAssignmentResponse(savedBranch);
    }

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse removeManager(String branchId) {
        OrganizationBranch branch = findOwnedBranch(branchId);

        if (branch.getManager() == null) {
            return employeeMapper.toEmployeeBranchAssignmentResponse(branch);
        }

        branch.setManager(null);
        OrganizationBranch savedBranch = branchRepository.saveAndFlush(branch);

        return employeeMapper.toEmployeeBranchAssignmentResponse(savedBranch);
    }

    private OrganizationBranch findOwnedBranch(String branchId) {
        return branchRepository.findByIdAndOwnerIdWithManager(branchId, AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private String extractManagerId(EmployeeBranchAssignmentRequest request) {
        if (request == null || !StringUtils.hasText(request.getManagerId())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST);
        }

        return request.getManagerId().trim();
    }

    private void validateAssignableManager(Employee employee, OrganizationBranch targetBranch) {
        validateEmployeeBranch(employee, targetBranch);
        validateManagerRole(employee);
        validateManagerStatus(employee);
        validateManagerUser(employee);
    }

    private void validateEmployeeBranch(Employee employee, OrganizationBranch targetBranch) {
        OrganizationBranch employeeBranch = employee.getBranch();
        Organization targetOrganization = targetBranch.getOrganization();
        Organization employeeOrganization = employeeBranch == null
                ? null
                : employeeBranch.getOrganization();

        if (employeeBranch == null
                || targetOrganization == null
                || employeeOrganization == null
                || !Objects.equals(employeeBranch.getId(), targetBranch.getId())
                || !Objects.equals(employeeOrganization.getId(), targetOrganization.getId())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH);
        }
    }

    private void validateManagerRole(Employee employee) {
        if (employee.getOrgRole() == null
                || !StartDefinedOrgRole.MANAGER.equals(employee.getOrgRole().getRoleName())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_ROLE);
        }
    }

    private void validateManagerStatus(Employee employee) {
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }

        if (employee.getEndDate() != null && employee.getEndDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_EXPIRED);
        }
    }

    private void validateManagerUser(Employee employee) {
        User user = employee.getUser();

        if (user == null || !user.isEnabled() || user.getStatus() != UserStatus.ACTIVE) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }
    }

    private boolean hasSameManager(OrganizationBranch targetBranch, Employee branchManager) {
        return targetBranch.getManager() != null
                && Objects.equals(targetBranch.getManager().getId(), branchManager.getId());
    }

    private void clearManagerFromOtherBranch(String employeeId, String targetBranchId) {
        branchRepository.findByManager_Id(employeeId)
                .filter(currentBranch -> !Objects.equals(currentBranch.getId(), targetBranchId))
                .ifPresent(currentBranch -> {
                    currentBranch.setManager(null);
                    branchRepository.saveAndFlush(currentBranch);
                });
    }
}
