package com.restaurant.crm.modules.erp.manage_branch_manager.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.shared.constants.org_permission.PredefinedOrgPermission;
import com.restaurant.crm.modules.erp.shared.constants.org_role.PredefinedOrgRole;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.request.BranchManagerCreationRequest;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.request.BranchManagerUpdateRequest;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.shared.entity.Employee;
import com.restaurant.crm.modules.erp.shared.entity.OrgPermission;
import com.restaurant.crm.modules.erp.shared.entity.OrgRole;
import com.restaurant.crm.modules.erp.shared.entity.Organization;
import com.restaurant.crm.modules.erp.shared.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.shared.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.manage_branch_manager.mapper.BranchManagerMapper;
import com.restaurant.crm.modules.erp.shared.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.shared.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.manage_branch_manager.service.interfaces.BranchManagerService;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class BranchManagerServiceImpl implements BranchManagerService {

    EmployeeRepository employeeRepository;
    OrganizationBranchRepository branchRepository;
    OrgRoleRepository orgRoleRepository;
    OrgPermissionRepository orgPermissionRepository;
    UserRepository userRepository;
    RoleRepository systemRoleRepository;
    PasswordEncoder passwordEncoder;
    BranchManagerMapper branchManagerMapper;

    @Override
    @Transactional
    public BranchManagerResponse create(BranchManagerCreationRequest request) {
        validateUsernameAvailable(request.getUsername());
        validateEmailAvailable(request.getEmail(), null);

        String ownerId = getCurrentOwnerId();
        OrganizationBranch branch = findBranch(request.getBranchId(), ownerId);
        ensureBranchHasNoOtherActiveManager(branch.getId(), null);

        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .email(request.getEmail())
                .enabled(true)
                .status(UserStatus.ACTIVE)
                .roles(getDefaultSystemRoles())
                .build();
        User savedUser = userRepository.save(user);

        Employee employee = Employee.builder()
                .user(savedUser)
                .branch(branch)
                .orgRole(getOrCreateBranchManagerRole(branch.getOrganization()))
                .fullName(request.getFullName())
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .build();
        Employee savedEmployee = employeeRepository.save(employee);

        branch.setManagerId(savedEmployee.getId());
        branchRepository.save(branch);

        return branchManagerMapper.toBranchManagerResponse(savedEmployee);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<BranchManagerResponse> getBranchManagers(PagingRequest request, String branchId) {
        String ownerId = getCurrentOwnerId();
        Pageable pageable = PageRequest.of(
                request.getPage() - GlobalVariableConstant.PAGE_SIZE_INDEX,
                request.getPageSize(),
                PagingUtil.createSort(request)
        );

        Page<Employee> employeePage = StringUtils.hasText(branchId)
                ? employeeRepository.findAllByOrgRole_RoleNameAndBranch_IdAndBranch_Organization_OwnerId(
                        PredefinedOrgRole.BRANCH_MANAGER,
                        branchId,
                        ownerId,
                        pageable)
                : employeeRepository.findAllByOrgRole_RoleNameAndBranch_Organization_OwnerId(
                        PredefinedOrgRole.BRANCH_MANAGER,
                        ownerId,
                        pageable);

        return PagingResponse.<BranchManagerResponse>builder()
                .currentPage(request.getPage())
                .pageSize(employeePage.getSize())
                .totalPages(employeePage.getTotalPages())
                .totalElement(employeePage.getTotalElements())
                .data(employeePage.getContent().stream()
                        .map(branchManagerMapper::toBranchManagerResponse)
                        .toList())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public BranchManagerResponse getById(String branchManagerId) {
        Employee employee = findBranchManager(branchManagerId, getCurrentOwnerId());
        return branchManagerMapper.toBranchManagerResponse(employee);
    }

    @Override
    @Transactional
    public BranchManagerResponse update(String branchManagerId, BranchManagerUpdateRequest request) {
        String ownerId = getCurrentOwnerId();
        Employee employee = findBranchManager(branchManagerId, ownerId);
        User user = employee.getUser();

        if (StringUtils.hasText(request.getPassword())) {
            user.setPassword(passwordEncoder.encode(request.getPassword()));
        }

        if (StringUtils.hasText(request.getEmail()) && !request.getEmail().equals(user.getEmail())) {
            validateEmailAvailable(request.getEmail(), user.getId());
            user.setEmail(request.getEmail());
            employee.setEmail(request.getEmail());
        }

        if (StringUtils.hasText(request.getFullName())) {
            employee.setFullName(request.getFullName());
        }

        if (request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }

        if (request.getEnabled() != null) {
            user.setEnabled(request.getEnabled());
        }

        EmployeeStatus targetStatus = request.getStatus() == null ? employee.getStatus() : request.getStatus();
        employee.setStatus(targetStatus);

        syncBranchManagerAssignment(employee, targetStatus);

        userRepository.save(user);
        Employee updated = employeeRepository.save(employee);
        return branchManagerMapper.toBranchManagerResponse(updated);
    }

    @Override
    @Transactional
    public void deleteById(String branchManagerId) {
        Employee employee = findBranchManager(branchManagerId, getCurrentOwnerId());
        employee.setStatus(EmployeeStatus.INACTIVE);
        employee.setEndDate(LocalDate.now());
        employee.getUser().setEnabled(false);

        OrganizationBranch branch = employee.getBranch();
        if (employee.getId().equals(branch.getManagerId())) {
            branch.setManagerId(null);
            branchRepository.save(branch);
        }

        userRepository.save(employee.getUser());
        employeeRepository.save(employee);
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

    private Set<Role> getDefaultSystemRoles() {
        Role userRole = systemRoleRepository.findByRoleName(PredefinedRole.USER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.ROLE_NOT_FOUND));
        return new HashSet<>(Set.of(userRole));
    }

    private void validateUsernameAvailable(String username) {
        if (userRepository.existsByUsername(username)) {
            throw new AppException(ErrorCode.USER_USERNAME_ALREADY_EXISTS);
        }
    }

    private void validateEmailAvailable(String email, String currentUserId) {
        userRepository.findByEmail(email).ifPresent(existingUser -> {
            if (currentUserId == null || !existingUser.getId().equals(currentUserId)) {
                throw new AppException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        });
    }

    private void syncBranchManagerAssignment(Employee employee, EmployeeStatus targetStatus) {
        OrganizationBranch branch = employee.getBranch();
        if (EmployeeStatus.ACTIVE.equals(targetStatus)) {
            ensureBranchHasNoOtherActiveManager(branch.getId(), employee.getId());
            branch.setManagerId(employee.getId());
        } else if (employee.getId().equals(branch.getManagerId())) {
            branch.setManagerId(null);
        }
        branchRepository.save(branch);
    }

    private void ensureBranchHasNoOtherActiveManager(String branchId, String currentEmployeeId) {
        boolean existed = currentEmployeeId == null
                ? employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatus(
                        branchId,
                        PredefinedOrgRole.BRANCH_MANAGER,
                        EmployeeStatus.ACTIVE)
                : employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatusAndIdNot(
                        branchId,
                        PredefinedOrgRole.BRANCH_MANAGER,
                        EmployeeStatus.ACTIVE,
                        currentEmployeeId);

        if (existed) {
            throw new AppException(ErrorCode.BRANCH_MANAGER_ALREADY_ASSIGNED);
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
