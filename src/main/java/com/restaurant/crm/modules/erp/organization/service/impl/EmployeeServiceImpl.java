package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeAccountConstants;
import com.restaurant.crm.modules.erp.organization.constants.OrgRoleConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.security.EmployeeBranchGuard;
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
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

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

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
    EmployeeBranchGuard employeeBranchGuard;
    UserProfileRepository userProfileRepository;

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeResponse> listEmployees() {
        OrgDataScope scope = AuthUtils.getDataScope();
        List<Employee> employees;
        if (scope == OrgDataScope.ORGANIZATION) {
            employees = new java.util.ArrayList<>(
                    employeeRepository.findByBranch_Organization_Id(AuthUtils.getOrganizationId()));
            // Owner Employee has branch=null, query separately by organization
            employees.addAll(employeeRepository.findByOrganization_Id(AuthUtils.getOrganizationId()));
        } else {
            employees = employeeRepository.findByBranch_Id(AuthUtils.getBranchId());
        }
        String currentEmployeeId = AuthUtils.getEmployeeId();

        List<Employee> visible = employees.stream()
                .filter(employee -> !employee.getId().equals(currentEmployeeId))
                .filter(employee -> employee.getOrgRole() == null
                        || !OrgRoleConstants.OWNER_ROLE.equals(employee.getOrgRole().getRoleName()))
                .toList();

        List<String> userIds = visible.stream()
                .map(employee -> employee.getUser().getId())
                .toList();
        Map<String, String> fullNameByUserId = userProfileRepository.findByUser_IdIn(userIds).stream()
                .filter(profile -> profile.getFullName() != null)
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        UserProfile::getFullName,
                        (existing, ignored) -> existing));

        return visible.stream()
                .map(employee -> {
                    EmployeeResponse response = employeeMapper.toEmployeeResponse(employee);
                    response.setFullName(fullNameByUserId.get(employee.getUser().getId()));
                    return response;
                })
                .toList();
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        employeeBranchGuard.validateBranchAccess(employee.getBranch().getId());

        User user = employee.getUser();
        UserProfile profile = userProfileRepository.findByUser_Id(user.getId())
                .orElseGet(() -> UserProfile.builder().user(user).build());

        if (request.getFullName() != null) {
            profile.setFullName(request.getFullName());
        }
        if (request.getPhone() != null) {
            if (!request.getPhone().equals(profile.getPhone())
                    && userProfileRepository.existsByPhone(request.getPhone())) {
                throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
            }
            profile.setPhone(request.getPhone());
            employee.setPhone(request.getPhone());
        }
        if (request.getStatus() != null) {
            // Chi activate duoc khi da co org role, neu khong selectContext se fail sau do
            if (request.getStatus() == EmployeeStatus.ACTIVE && employee.getOrgRole() == null) {
                throw new AppException(ErrorCode.EMPLOYEE_ACTIVATE_REQUIRES_ORG_ROLE);
            }
            employee.setStatus(request.getStatus());
        }
        if (request.getStartDate() != null) {
            employee.setStartDate(request.getStartDate());
        }
        if (request.getEndDate() != null) {
            employee.setEndDate(request.getEndDate());
        }

        userProfileRepository.save(profile);
        return toResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        employeeBranchGuard.validateBranchAccess(request.getBranchId());

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }
        if (request.getPhone() != null && userProfileRepository.existsByPhone(request.getPhone())) {
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }

        OrganizationBranch branch = branchRepository.findById(request.getBranchId())
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

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

        userProfileRepository.save(UserProfile.builder()
                .user(user)
                .fullName(request.getFullName())
                .phone(request.getPhone())
                .build());

        // Gan role di duong rieng (assignRole), sau do moi activate qua updateEmployee.
        Employee employee = Employee.builder()
                .user(user)
                .branch(branch)
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(request.getStartDate())
                .salary(request.getSalary())
                .status(EmployeeAccountConstants.DEFAULT_STATUS)
                .build();
        employee = employeeRepository.save(employee);

        return toResponse(employee);
    }

    private EmployeeResponse toResponse(Employee employee) {
        EmployeeResponse response = employeeMapper.toEmployeeResponse(employee);
        userProfileRepository.findByUser_Id(employee.getUser().getId())
                .ifPresent(profile -> response.setFullName(profile.getFullName()));
        return response;
    }

    private void rejectSelfRoleChange(Employee employee) {
        if (employee.getId().equals(AuthUtils.getEmployeeId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
    }

    @Override
    @Transactional
    public EmployeeResponse assignRole(String employeeId, AssignRoleRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeBranchGuard.validateBranchAccess(employee.getBranch().getId());
        rejectSelfRoleChange(employee);

        OrgRole orgRole = orgRoleRepository.findById(request.getOrgRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));

        employee.setOrgRole(orgRole);
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse revokeRole(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeBranchGuard.validateBranchAccess(employee.getBranch().getId());
        rejectSelfRoleChange(employee);

        // idempotent: neu da khong co role thi tra ve binh thuong
        employee.setOrgRole(null);
        // ACTIVE luon phai di kem org role, go role thi ha xuong INACTIVE.
        // TERMINATED giu nguyen vi day la trang thai cuoi, khong ha xuong INACTIVE.
        if (employee.getStatus() == EmployeeStatus.ACTIVE) {
            employee.setStatus(EmployeeStatus.INACTIVE);
        }
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        employeeBranchGuard.validateBranchAccess(employee.getBranch().getId());

        employee.setSalary(request.getSalary());
        employee = employeeRepository.save(employee);
        return toResponse(employee);
    }

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
