package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeAccountConstants;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    EmployeeMapper employeeMapper;
    UserRepository userRepository;
    RoleRepository roleRepository;
    OrgRoleRepository orgRoleRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        validateBranchAccess(request.getBranchId());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        OrganizationBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        OrgRole orgRole = null;
        if (request.getOrgRoleId() != null && !request.getOrgRoleId().isBlank()) {
            orgRole = orgRoleRepository.findById(request.getOrgRoleId())
                    .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));
        }

        Role userRole = roleRepository.findByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(EmployeeAccountConstants.DEFAULT_PASSWORD))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(user)
                .branch(branch)
                .orgRole(orgRole)
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(request.getStartDate())
                .salary(request.getSalary())
                .status(EmployeeStatus.ACTIVE)
                .build();
        employee = employeeRepository.save(employee);

        return employeeMapper.toEmployeeResponse(employee);
    }

    private void validateBranchAccess(String targetBranchId) {
        String actorUserId = AuthUtils.getCurrentUserId();
        if (AuthUtils.getEmployeeId() == null) {
            branchRepository.findByIdAndOrganization_Owner_Id(targetBranchId, actorUserId)
                    .orElseThrow(() -> new AppException(ErrorCode.AUTHZ_UNAUTHORIZED));
        } else if (!targetBranchId.equals(AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public EmployeeResponse assignRole(String employeeId, AssignRoleRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        validateBranchAccess(employee.getBranch().getId());

        OrgRole orgRole = orgRoleRepository.findById(request.getOrgRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));

        employee.setOrgRole(orgRole);
        employee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse revokeRole(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        validateBranchAccess(employee.getBranch().getId());

        // idempotent: neu da khong co role thi tra ve binh thuong
        employee.setOrgRole(null);
        employee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        validateBranchAccess(employee.getBranch().getId());

        employee.setSalary(request.getSalary());
        employee = employeeRepository.save(employee);
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request) {
        String ownerId = getCurrentOwnerId();
        OrganizationBranch targetBranch = findBranch(branchId, ownerId);
        Employee branchManager = findManagerEmployee(request.getManagerId());

        validateManagerForBranch(branchManager, targetBranch);

        String managerEmployeeId = branchManager.getId();
        if (targetBranch.getManager() != null && managerEmployeeId.equals(targetBranch.getManager().getId())) {
            return employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch);
        }

        clearManagerFromOtherBranch(managerEmployeeId, targetBranch.getId());

        targetBranch.setManager(branchManager);

        OrganizationBranch savedBranch = branchRepository.saveAndFlush(targetBranch);
        return employeeMapper.toEmployeeBranchAssignmentResponse(savedBranch);
    }

    private Employee findManagerEmployee(String managerId) {
        if (!StringUtils.hasText(managerId)) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST);
        }
        return employeeRepository.findByIdWithUserRoleAndBranch(managerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private void validateManagerForBranch(Employee employee, OrganizationBranch targetBranch) {
        if (employee.getBranch() == null || employee.getBranch().getOrganization() == null) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH);
        }
        if (!targetBranch.getId().equals(employee.getBranch().getId())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH);
        }
        if (!targetBranch.getOrganization().getId().equals(employee.getBranch().getOrganization().getId())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH);
        }
        if (!isManagerRole(employee)) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_ROLE);
        }
        if (!EmployeeStatus.ACTIVE.equals(employee.getStatus())
                || employee.getUser() == null
                || !employee.getUser().isEnabled()
                || !UserStatus.ACTIVE.equals(employee.getUser().getStatus())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INACTIVE);
        }
        if (employee.getEndDate() != null && employee.getEndDate().isBefore(LocalDate.now())) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_EXPIRED);
        }
    }

    private boolean isManagerRole(Employee employee) {
        return employee.getOrgRole() != null
                && EmployeeConstants.MANAGER_ROLE_NAME.equals(employee.getOrgRole().getRoleName());
    }

    private void clearManagerFromOtherBranch(String employeeId, String targetBranchId) {
        branchRepository.findByManager_Id(employeeId).ifPresent(currentBranch -> {
            if (!targetBranchId.equals(currentBranch.getId())) {
                currentBranch.setManager(null);
                branchRepository.saveAndFlush(currentBranch);
            }
        });
    }

    private String getCurrentOwnerId() {
        String ownerId = AuthUtils.getCurrentUserId();
        if (!StringUtils.hasText(ownerId)) {
            throw new AppException(ErrorCode.AUTH_UNAUTHENTICATED);
        }
        return ownerId;
    }
}
