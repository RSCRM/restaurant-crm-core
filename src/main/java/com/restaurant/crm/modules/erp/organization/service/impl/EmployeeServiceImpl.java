package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgRole;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
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
import jakarta.persistence.criteria.JoinType;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Locale;
import java.util.Objects;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmployeeServiceImpl implements EmployeeService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    OrgRoleRepository orgRoleRepository;
    UserRepository userRepository;
    RoleRepository roleRepository;
    EmployeeMapper employeeMapper;
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
        EmployeeScope scope = resolveScope(organizationId, branchId);
        Pageable pageable = PageRequest.of(
                Math.max(page, GlobalVariableConstant.PAGE_SIZE_INDEX) - GlobalVariableConstant.PAGE_SIZE_INDEX,
                Math.max(size, 1),
                createEmployeeSort(field, direction)
        );

        Page<Employee> employeePage = employeeRepository.findAll(
                employeeSpecification(scope, keyword, role, status),
                pageable
        );

        return PagingResponse.<EmployeeResponse>builder()
                .currentPage(page)
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .totalElement(employeePage.getTotalElements())
                .data(employeePage.getContent()
                        .stream()
                        .map(employeeMapper::toEmployeeResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeResponse getEmployee(String employeeId) {
        Employee employee = findEmployeeInScope(employeeId);
        return employeeMapper.toEmployeeResponse(employee);
    }

    @Override
    @Transactional
    public EmployeeResponse createEmployee(CreateEmployeeRequest request) {
        EmployeeScope scope = resolveScope(null, request.getBranchId());
        OrganizationBranch branch = findBranchInScope(request.getBranchId(), scope);
        OrgRole orgRole = findOrgRole(request.getOrgRoleId(), request.getOrgRoleName(), request.getRole());

        validateUsernameAvailable(request.getUsername(), null);
        validateEmailAvailable(request.getEmail(), null);

        Role staffRole = roleRepository.findByRoleName(PredefinedRole.STAFF_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));

        User user = User.builder()
                .username(request.getUsername().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail().trim())
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .roles(new HashSet<>(Set.of(staffRole)))
                .build();
        User savedUser = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(savedUser)
                .orgRole(orgRole)
                .branch(branch)
                .firstName(request.getFirstName().trim())
                .lastName(request.getLastName().trim())
                .email(request.getEmail().trim())
                .phone(cleanText(request.getPhone()))
                .status(request.getStatus())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .build();

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request) {
        Employee employee = findEmployeeInScope(employeeId);
        EmployeeScope scope = resolveScope(null, request.getBranchId());
        OrganizationBranch branch = findBranchInScope(request.getBranchId(), scope);
        OrgRole orgRole = findOrgRole(request.getOrgRoleId(), request.getOrgRoleName(), request.getRole());
        User user = employee.getUser();

        validateUsernameAvailable(request.getUsername(), user == null ? null : user.getId());
        validateEmailAvailable(request.getEmail(), user == null ? null : user.getId());

        if (user != null) {
            user.setUsername(request.getUsername().trim());
            user.setEmail(request.getEmail().trim());
            if (StringUtils.hasText(request.getPassword())) {
                user.setPassword(passwordEncoder.encode(request.getPassword()));
            }
        }

        employee.setFirstName(request.getFirstName().trim());
        employee.setLastName(request.getLastName().trim());
        employee.setEmail(request.getEmail().trim());
        employee.setPhone(cleanText(request.getPhone()));
        employee.setBranch(branch);
        employee.setOrgRole(orgRole);
        employee.setStatus(request.getStatus());
        employee.setStartDate(request.getStartDate());
        employee.setEndDate(request.getEndDate());

        clearManagerIfNoLongerAssignable(employee);

        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse softDeleteEmployee(String employeeId) {
        Employee employee = findEmployeeInScope(employeeId);
        employee.setStatus(EmployeeStatus.TERMINATED);
        employee.setEndDate(LocalDate.now());
        if (employee.getUser() != null) {
            employee.getUser().setEnabled(false);
            employee.getUser().setStatus(UserStatus.DELETED);
        }
        clearManagerFromOtherBranch(employee.getId(), null);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse enableEmployee(String employeeId) {
        Employee employee = findEmployeeInScope(employeeId);
        if (employee.getUser() != null) {
            employee.getUser().setEnabled(true);
            employee.getUser().setStatus(UserStatus.ACTIVE);
        }
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

    @Override
    @Transactional
    public EmployeeResponse disableEmployee(String employeeId) {
        Employee employee = findEmployeeInScope(employeeId);
        if (employee.getUser() != null) {
            employee.getUser().setEnabled(false);
            employee.getUser().setStatus(UserStatus.BLOCKED);
        }
        clearManagerFromOtherBranch(employee.getId(), null);
        return employeeMapper.toEmployeeResponse(employeeRepository.save(employee));
    }

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
        EmployeeScope scope = resolveScope(null, branchId);
        return branchRepository.findByIdAndOrganizationIdWithManager(branchId, scope.organizationId())
                .filter(branch -> !StringUtils.hasText(scope.branchId())
                        || Objects.equals(branch.getId(), scope.branchId()))
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

    private Employee findEmployeeInScope(String employeeId) {
        Employee employee = employeeRepository.findByIdWithDetails(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        EmployeeScope scope = resolveScope(
                employee.getBranch() == null || employee.getBranch().getOrganization() == null
                        ? null
                        : employee.getBranch().getOrganization().getId(),
                employee.getBranch() == null ? null : employee.getBranch().getId()
        );

        if (!isEmployeeInScope(employee, scope)) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }

        return employee;
    }

    private EmployeeScope resolveScope(String requestedOrganizationId, String requestedBranchId) {
        String claimOrganizationId = AuthUtils.getOrganizationId();
        String claimBranchId = AuthUtils.getBranchId();
        String organizationId = StringUtils.hasText(requestedOrganizationId)
                ? requestedOrganizationId.trim()
                : claimOrganizationId;

        if (!StringUtils.hasText(claimOrganizationId)) {
            throw new AppException(ErrorCode.JWT_CLAIM_MISSING);
        }

        if (StringUtils.hasText(requestedOrganizationId)
                && !Objects.equals(claimOrganizationId, requestedOrganizationId.trim())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }

        String branchId = StringUtils.hasText(claimBranchId)
                ? claimBranchId
                : cleanText(requestedBranchId);

        if (StringUtils.hasText(claimBranchId)
                && StringUtils.hasText(requestedBranchId)
                && !Objects.equals(claimBranchId, requestedBranchId.trim())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }

        return new EmployeeScope(organizationId, branchId);
    }

    private Specification<Employee> employeeSpecification(
            EmployeeScope scope,
            String keyword,
            String role,
            String status
    ) {
        return (root, query, criteriaBuilder) -> {
            var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();
            var user = root.join("user", JoinType.LEFT);
            var orgRole = root.join("orgRole", JoinType.LEFT);
            var branch = root.join("branch", JoinType.LEFT);
            var organization = branch.join("organization", JoinType.LEFT);

            predicates.add(criteriaBuilder.equal(organization.get("id"), scope.organizationId()));

            if (StringUtils.hasText(scope.branchId())) {
                predicates.add(criteriaBuilder.equal(branch.get("id"), scope.branchId()));
            }

            if (StringUtils.hasText(keyword)) {
                String likeKeyword = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                predicates.add(criteriaBuilder.or(
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("id")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("firstName")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("lastName")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(root.get("phone")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(user.get("username")), likeKeyword),
                        criteriaBuilder.like(criteriaBuilder.lower(branch.get("branchName")), likeKeyword)
                ));
            }

            if (StringUtils.hasText(role)) {
                predicates.add(criteriaBuilder.equal(orgRole.get("roleName"), role.trim()));
            }

            EmployeeStatus employeeStatus = parseEmployeeStatus(status);
            if (employeeStatus != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), employeeStatus));
            }

            return criteriaBuilder.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };
    }

    private boolean isEmployeeInScope(Employee employee, EmployeeScope scope) {
        OrganizationBranch branch = employee.getBranch();
        Organization organization = branch == null ? null : branch.getOrganization();

        if (organization == null || !Objects.equals(organization.getId(), scope.organizationId())) {
            return false;
        }

        return !StringUtils.hasText(scope.branchId())
                || (branch != null && Objects.equals(branch.getId(), scope.branchId()));
    }

    private OrganizationBranch findBranchInScope(String branchId, EmployeeScope scope) {
        OrganizationBranch branch = branchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        Organization organization = branch.getOrganization();

        if (organization == null
                || !Objects.equals(organization.getId(), scope.organizationId())
                || (StringUtils.hasText(scope.branchId()) && !Objects.equals(branch.getId(), scope.branchId()))) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }

        return branch;
    }

    private OrgRole findOrgRole(String orgRoleId, String orgRoleName, String role) {
        if (StringUtils.hasText(orgRoleId)) {
            return orgRoleRepository.findById(orgRoleId.trim())
                    .or(() -> orgRoleRepository.findByRoleName(orgRoleId.trim()))
                    .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        }

        String roleName = StringUtils.hasText(orgRoleName) ? orgRoleName : role;
        if (!StringUtils.hasText(roleName)) {
            throw new AppException(ErrorCode.EMPLOYEE_ROLE_REQUIRED);
        }

        return orgRoleRepository.findByRoleName(roleName.trim())
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
    }

    private EmployeeStatus parseEmployeeStatus(String status) {
        if (!StringUtils.hasText(status)) {
            return null;
        }

        try {
            return EmployeeStatus.valueOf(status.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new AppException(ErrorCode.EMPLOYEE_STATUS_INVALID);
        }
    }

    private Sort createEmployeeSort(String field, String direction) {
        String sortField = switch (StringUtils.hasText(field) ? field.trim() : "createdAt") {
            case "firstName", "lastName", "email", "phone", "status", "startDate", "endDate", "createdAt", "updatedAt" -> field.trim();
            default -> "createdAt";
        };

        Sort.Direction sortDirection = Sort.Direction.DESC;
        if (StringUtils.hasText(direction)) {
            try {
                sortDirection = Sort.Direction.fromString(direction);
            } catch (IllegalArgumentException ignored) {
                sortDirection = Sort.Direction.DESC;
            }
        }

        return Sort.by(sortDirection, sortField);
    }

    private void validateUsernameAvailable(String username, String currentUserId) {
        userRepository.findByUsername(username.trim())
                .filter(user -> !Objects.equals(user.getId(), currentUserId))
                .ifPresent(_user -> {
                    throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
                });
    }

    private void validateEmailAvailable(String email, String currentUserId) {
        userRepository.findByEmail(email.trim())
                .filter(user -> !Objects.equals(user.getId(), currentUserId))
                .ifPresent(_user -> {
                    throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
                });
    }

    private void clearManagerIfNoLongerAssignable(Employee employee) {
        if (employee.getStatus() != EmployeeStatus.ACTIVE
                || employee.getOrgRole() == null
                || !StartDefinedOrgRole.MANAGER.equals(employee.getOrgRole().getRoleName())) {
            clearManagerFromOtherBranch(employee.getId(), null);
        }
    }

    private String cleanText(String value) {
        return StringUtils.hasText(value) ? value.trim() : null;
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

    private record EmployeeScope(String organizationId, String branchId) {}
}
