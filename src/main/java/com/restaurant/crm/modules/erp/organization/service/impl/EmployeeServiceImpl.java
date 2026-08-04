package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.ProfileUpdateAccessRequest;
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
import com.restaurant.crm.modules.erp.organization.service.interfaces.EmployeeService;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
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
    UserProfileRepository userProfileRepository;
    PasswordEncoder passwordEncoder;

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<EmployeeResponse> getEmployees(
            String organizationId,
            String branchId,
            String keyword,
            String role,
            String status,
            int page,
            int size,
            String field,
            String direction
    ) {
        String resolvedOrganizationId = resolveOrganizationId(organizationId);
        String resolvedBranchId = resolveBranchId(branchId);
        EmployeeStatus employeeStatus = parseEmployeeStatus(status);
        Pageable pageable = PageRequest.of(page - GlobalVariableConstant.PAGE_SIZE_INDEX, size);

        Page<Employee> employeePage = employeeRepository.searchByOrganization(
                resolvedOrganizationId,
                resolvedBranchId,
                normalizeFilter(keyword),
                normalizeFilter(role),
                employeeStatus,
                pageable
        );

        return PagingResponse.<EmployeeResponse>builder()
                .currentPage(page)
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .totalElement(employeePage.getTotalElements())
                .data(toEmployeeResponses(employeePage.getContent()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(String employeeId) {
        Employee employee = findEmployeeForCurrentContext(employeeId);
        return toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse addEmployee(CreateEmployeeRequest request) {
        OrganizationBranch branch = resolveBranchForCreateOrUpdate(request.getBranchId());
        OrgRole orgRole = findOrgRole(request.getOrgRoleId());
        validateProfilePhoneAvailable(request.getPhone(), null);

        String username = cleanRequired(request.getUsername(), ErrorCode.EMPLOYEE_USERNAME_REQUIRED);
        String email = cleanRequired(request.getEmail(), ErrorCode.EMPLOYEE_EMAIL_REQUIRED);
        String password = cleanRequired(request.getPassword(), ErrorCode.USER_PASSWORD_INVALID);
        String phone = cleanRequired(request.getPhone(), ErrorCode.EMPLOYEE_PHONE_REQUIRED);
        String fullName = cleanRequired(request.getFullName(), ErrorCode.USER_FULL_NAME_INVALID);

        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
        if (userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        Role userRole = roleRepository.findByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(username)
                .email(email)
                .password(passwordEncoder.encode(password))
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .roles(Set.of(userRole))
                .build();
        user = userRepository.save(user);

        UserProfile profile = UserProfile.builder()
                .user(user)
                .fullName(fullName)
                .phone(phone)
                .build();
        profile = userProfileRepository.save(profile);

        Employee employee = Employee.builder()
                .user(user)
                .branch(branch)
                .orgRole(orgRole)
                .email(email)
                .phone(phone)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .salary(request.getSalary())
                .status(request.getStatus() == null ? EmployeeStatus.ACTIVE : request.getStatus())
                .build();
        employee = employeeRepository.save(employee);

        return toEmployeeResponse(employee, profile);
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        Employee employee = findEmployeeForCurrentContext(employeeId);
        OrganizationBranch branch = resolveBranchForCreateOrUpdate(request.getBranchId());
        OrgRole orgRole = findOrgRole(request.getOrgRoleId());

        UserProfile profile = userProfileRepository.findByUser_Id(employee.getUser().getId())
                .orElseGet(() -> UserProfile.builder().user(employee.getUser()).build());
        validateProfilePhoneAvailable(request.getPhone(), profile.getId());

        String username = cleanRequired(request.getUsername(), ErrorCode.EMPLOYEE_USERNAME_REQUIRED);
        String email = cleanRequired(request.getEmail(), ErrorCode.EMPLOYEE_EMAIL_REQUIRED);
        String phone = cleanRequired(request.getPhone(), ErrorCode.EMPLOYEE_PHONE_REQUIRED);
        String fullName = cleanRequired(request.getFullName(), ErrorCode.USER_FULL_NAME_INVALID);

        if (!employee.getUser().getUsername().equals(username)
                && userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
        if (!employee.getUser().getEmail().equals(email)
                && userRepository.existsByEmail(email)) {
            throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
        }

        if (StringUtils.hasText(request.getPassword())) {
            employee.getUser().setPassword(passwordEncoder.encode(request.getPassword().trim()));
        }

        employee.getUser().setUsername(username);
        employee.getUser().setEmail(email);
        employee.setEmail(email);
        employee.setPhone(phone);
        employee.setBranch(branch);
        employee.setOrgRole(orgRole);
        employee.setStartDate(request.getStartDate());
        employee.setEndDate(request.getEndDate());
        employee.setSalary(request.getSalary());
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }

        userRepository.save(employee.getUser());
        profile.setFullName(fullName);
        profile.setPhone(phone);
        profile = userProfileRepository.save(profile);
        return toEmployeeResponse(employeeRepository.save(employee), profile);
    }

    @Override
    @Transactional
    public EmployeeResponse deleteEmployee(String employeeId) {
        Employee employee = findEmployeeForCurrentContext(employeeId);
        employee.setStatus(EmployeeStatus.TERMINATED);
        employee.setEndDate(LocalDate.now());
        if (employee.getUser() != null) {
            employee.getUser().setEnabled(false);
            employee.getUser().setStatus(UserStatus.BLOCKED);
            userRepository.save(employee.getUser());
        }
        clearManagerFromOtherBranch(employee.getId(), null);
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse enableEmployee(String employeeId) {
        Employee employee = findEmployeeForCurrentContext(employeeId);
        employee.getUser().setEnabled(true);
        employee.getUser().setStatus(UserStatus.ACTIVE);
        employee.setStatus(EmployeeStatus.ACTIVE);
        userRepository.save(employee.getUser());
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse disableEmployee(String employeeId) {
        Employee employee = findEmployeeForCurrentContext(employeeId);
        employee.getUser().setEnabled(false);
        employee.getUser().setStatus(UserStatus.BLOCKED);
        if (EmployeeStatus.ACTIVE.equals(employee.getStatus())) {
            employee.setStatus(EmployeeStatus.INACTIVE);
        }
        clearManagerFromOtherBranch(employee.getId(), null);
        userRepository.save(employee.getUser());
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    private void validateBranchAccess(String targetBranchId) {
        findBranchForCurrentContext(targetBranchId);
    }

    private OrganizationBranch resolveBranchForCreateOrUpdate(String requestedBranchId) {
        OrgDataScope dataScope = AuthUtils.getDataScope();
        if (OrgDataScope.BRANCH.equals(dataScope)) {
            String contextBranchId = AuthUtils.getBranchId();
            if (!StringUtils.hasText(contextBranchId)) {
                throw new AppException(ErrorCode.EMPLOYEE_BRANCH_REQUIRED);
            }
            if (StringUtils.hasText(requestedBranchId) && !contextBranchId.equals(requestedBranchId)) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return findBranchForCurrentContext(contextBranchId);
        }

        if (!StringUtils.hasText(requestedBranchId)) {
            throw new AppException(ErrorCode.EMPLOYEE_BRANCH_REQUIRED);
        }

        return findBranchForCurrentContext(requestedBranchId);
    }

    private OrgRole findOrgRole(String orgRoleId) {
        if (!StringUtils.hasText(orgRoleId)) {
            throw new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_REQUIRED);
        }
        return orgRoleRepository.findById(orgRoleId.trim())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));
    }

    private void validateProfilePhoneAvailable(String phone, String currentProfileId) {
        if (!StringUtils.hasText(phone)) {
            throw new AppException(ErrorCode.EMPLOYEE_PHONE_REQUIRED);
        }
        if (!userProfileRepository.existsByPhone(phone.trim())) {
            return;
        }
        if (!StringUtils.hasText(currentProfileId)) {
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }
        UserProfile profile = userProfileRepository.findById(currentProfileId)
                .orElseThrow(() -> new AppException(ErrorCode.USER_NOT_FOUND));
        if (!phone.trim().equals(profile.getPhone())) {
            throw new AppException(ErrorCode.USER_PHONE_ALREADY_EXISTS);
        }
    }

    private String cleanRequired(String value, ErrorCode errorCode) {
        if (!StringUtils.hasText(value)) {
            throw new AppException(errorCode);
        }
        return value.trim();
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

        validateBranchAccess(employee.getBranch().getId());
        rejectSelfRoleChange(employee);

        OrgRole orgRole = orgRoleRepository.findById(request.getOrgRoleId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_ORG_ROLE_NOT_FOUND));

        employee.setOrgRole(orgRole);
        employee = employeeRepository.save(employee);
        return toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse revokeRole(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        validateBranchAccess(employee.getBranch().getId());
        rejectSelfRoleChange(employee);

        // idempotent: neu da khong co role thi tra ve binh thuong
        employee.setOrgRole(null);
        employee = employeeRepository.save(employee);
        return toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));

        validateBranchAccess(employee.getBranch().getId());

        employee.setSalary(request.getSalary());
        employee = employeeRepository.save(employee);
        return toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse setProfileUpdateAccess(
            String employeeId, ProfileUpdateAccessRequest request) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        validateBranchAccess(employee.getBranch().getId());
        employee.setProfileUpdateEnabled(request.getEnabled());
        return toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request) {
        OrganizationBranch targetBranch = findBranchForCurrentContext(branchId);
        Employee branchManager = findManagerEmployee(
                request.getManagerId(),
                targetBranch.getId(),
                targetBranch.getOrganization().getId()
        );

        validateManagerForBranch(branchManager, targetBranch);

        String managerEmployeeId = branchManager.getId();
        if (managerEmployeeId.equals(targetBranch.getManagerId())) {
            return toBranchAssignmentResponse(targetBranch, branchManager);
        }

        clearManagerFromOtherBranch(managerEmployeeId, targetBranch.getId());

        targetBranch.setManagerId(managerEmployeeId);

        OrganizationBranch savedBranch = branchRepository.saveAndFlush(targetBranch);
        return toBranchAssignmentResponse(savedBranch, branchManager);
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeBranchAssignmentResponse getBranchManager(String branchId) {
        OrganizationBranch branch = findBranchForCurrentContext(branchId);
        Employee manager = findBranchManager(branch.getManagerId());
        if (manager == null) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND);
        }
        return toBranchAssignmentResponse(branch, manager);
    }

    @Override
    @Transactional
    public EmployeeBranchAssignmentResponse removeBranchManager(String branchId) {
        OrganizationBranch branch = findBranchForCurrentContext(branchId);
        Employee manager = findBranchManager(branch.getManagerId());
        if (manager == null) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND);
        }
        branch.setManagerId(null);
        OrganizationBranch savedBranch = branchRepository.saveAndFlush(branch);
        return toBranchAssignmentResponse(savedBranch, manager);
    }

    private EmployeeBranchAssignmentResponse toBranchAssignmentResponse(
            OrganizationBranch branch,
            Employee manager
    ) {
        EmployeeBranchAssignmentResponse response = employeeMapper.toEmployeeBranchAssignmentResponse(branch, manager);
        if (manager.getUser() == null) {
            return response;
        }
        Optional.ofNullable(userProfileRepository.findByUser_Id(manager.getUser().getId()))
                .orElse(Optional.empty())
                .map(UserProfile::getFullName)
                .filter(StringUtils::hasText)
                .map(String::trim)
                .ifPresent(response::setManagerName);
        return response;
    }

    private Employee findManagerEmployee(String managerId, String branchId, String organizationId) {
        if (!StringUtils.hasText(managerId)) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST);
        }
        List<Employee> managers = employeeRepository.findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
                managerId,
                branchId,
                organizationId
        );

        if (managers.isEmpty()) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND);
        }
        if (managers.size() > 1) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST);
        }

        return managers.get(0);
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private Employee findBranchManager(String managerEmployeeId) {
        if (!StringUtils.hasText(managerEmployeeId)) {
            return null;
        }
        return employeeRepository.findByIdWithUserRoleAndBranch(managerEmployeeId)
                .orElse(null);
    }

    private OrganizationBranch findBranchForCurrentContext(String branchId) {
        if (!StringUtils.hasText(branchId)) {
            throw new AppException(ErrorCode.EMPLOYEE_BRANCH_REQUIRED);
        }
        String currentUserId = AuthUtils.getCurrentUserId();
        if (StringUtils.hasText(currentUserId)) {
            Optional<OrganizationBranch> ownerBranch =
                    branchRepository.findByIdAndOwnerIdWithManager(branchId, currentUserId);
            if (ownerBranch.isPresent()) {
                return ownerBranch.get();
            }
        }

        OrganizationBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));

        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            return branch;
        }

        OrgDataScope dataScope = AuthUtils.getDataScope();
        if (OrgDataScope.ORGANIZATION.equals(dataScope)
                && branch.getOrganization() != null
                && branch.getOrganization().getId().equals(AuthUtils.getOrganizationId())) {
            return branch;
        }
        if (OrgDataScope.BRANCH.equals(dataScope) && branchId.equals(AuthUtils.getBranchId())) {
            return branch;
        }

        throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
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
        branchRepository.findByManagerId(employeeId).forEach(currentBranch -> {
            if (targetBranchId == null || !targetBranchId.equals(currentBranch.getId())) {
                currentBranch.setManagerId(null);
                branchRepository.saveAndFlush(currentBranch);
            }
        });
    }

    private Employee findEmployeeForCurrentContext(String employeeId) {
        Employee employee = employeeRepository.findByIdWithUserRoleAndBranch(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        validateBranchAccess(employee.getBranch().getId());
        return employee;
    }

    private String resolveOrganizationId(String organizationId) {
        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)) {
            if (!StringUtils.hasText(organizationId)) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return organizationId;
        }

        OrgDataScope dataScope = AuthUtils.getDataScope();
        if (OrgDataScope.ORGANIZATION.equals(dataScope)) {
            String contextOrganizationId = AuthUtils.getOrganizationId();
            if (StringUtils.hasText(organizationId) && !organizationId.equals(contextOrganizationId)) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return contextOrganizationId;
        }
        if (OrgDataScope.BRANCH.equals(dataScope)) {
            OrganizationBranch branch = branchRepository.findById(AuthUtils.getBranchId())
                    .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
            if (StringUtils.hasText(organizationId) && !organizationId.equals(branch.getOrganization().getId())) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return branch.getOrganization().getId();
        }

        throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
    }

    private String resolveBranchId(String branchId) {
        if (AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)
                || OrgDataScope.ORGANIZATION.equals(AuthUtils.getDataScope())) {
            return normalizeFilter(branchId);
        }

        if (OrgDataScope.BRANCH.equals(AuthUtils.getDataScope())) {
            String contextBranchId = AuthUtils.getBranchId();
            if (StringUtils.hasText(branchId) && !branchId.equals(contextBranchId)) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
            return contextBranchId;
        }

        throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
    }

    private EmployeeStatus parseEmployeeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }
        try {
            return EmployeeStatus.valueOf(status);
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_FOUND);
        }
    }

    private String normalizeFilter(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
    }

    private List<EmployeeResponse> toEmployeeResponses(Collection<Employee> employees) {
        List<String> userIds = employees.stream()
                .map(Employee::getUser)
                .filter(user -> user != null && StringUtils.hasText(user.getId()))
                .map(User::getId)
                .toList();
        Map<String, UserProfile> profilesByUserId = userIds.isEmpty()
                ? Map.of()
                : userProfileRepository.findByUser_IdIn(userIds).stream()
                        .collect(Collectors.toMap(
                                profile -> profile.getUser().getId(),
                                profile -> profile,
                                (first, ignored) -> first));

        return employees.stream()
                .map(employee -> toEmployeeResponse(
                        employee,
                        employee.getUser() == null ? null : profilesByUserId.get(employee.getUser().getId())))
                .toList();
    }

    private EmployeeResponse toEmployeeResponse(Employee employee) {
        UserProfile profile = employee.getUser() == null
                ? null
                : userProfileRepository.findByUser_Id(employee.getUser().getId()).orElse(null);
        return toEmployeeResponse(employee, profile);
    }

    private EmployeeResponse toEmployeeResponse(Employee employee, UserProfile profile) {
        EmployeeResponse response = employeeMapper.toEmployeeResponse(employee);
        if (profile == null || !StringUtils.hasText(profile.getFullName())) {
            return response;
        }
        String fullName = profile.getFullName().trim();
        response.setFullName(fullName);
        if (!StringUtils.hasText(response.getPhone())) {
            response.setPhone(profile.getPhone());
        }
        return response;
    }
}
