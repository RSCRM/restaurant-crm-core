package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    EmployeeMapper employeeMapper;

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request) {
        String ownerId = getCurrentOwnerId();
        OrganizationBranch targetBranch = findBranch(branchId, ownerId);
        Employee branchManager = findEmployeeByManagerId(
                request.getManagerId(),
                targetBranch.getOrganization().getId(),
                ownerId
        );
        String managerId = branchManager.getUser().getId();

        if (!EmployeeStatus.ACTIVE.equals(branchManager.getStatus()) || !branchManager.getUser().isEnabled()) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }

        if (managerId.equals(targetBranch.getManagerId())) {
            return employeeMapper.toEmployeeBranchAssignmentResponse(branchManager);
        }

        ensureBranchHasNoOtherManager(targetBranch, managerId);
        clearCurrentBranchAssignment(branchManager);

        branchManager.setBranch(targetBranch);
        targetBranch.setManagerId(managerId);
        branchRepository.save(targetBranch);

        Employee assigned = employeeRepository.save(branchManager);
        return employeeMapper.toEmployeeBranchAssignmentResponse(assigned);
    }

    private Employee findEmployeeByManagerId(String managerId, String organizationId, String ownerId) {
        return employeeRepository.findByUser_IdAndBranch_Organization_IdAndBranch_Organization_OwnerId(
                        managerId,
                        organizationId,
                        ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOrganization_OwnerId(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private void ensureBranchHasNoOtherManager(OrganizationBranch targetBranch, String managerId) {
        if (StringUtils.hasText(targetBranch.getManagerId()) && !managerId.equals(targetBranch.getManagerId())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_ALREADY_ASSIGNED);
        }
    }

    private void clearCurrentBranchAssignment(Employee employee) {
        OrganizationBranch currentBranch = employee.getBranch();
        if (currentBranch != null && employee.getUser().getId().equals(currentBranch.getManagerId())) {
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
