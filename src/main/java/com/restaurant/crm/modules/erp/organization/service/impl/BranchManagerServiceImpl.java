package com.restaurant.crm.modules.erp.organization.service.impl;

import com.restaurant.crm.common.constant.GlobalVariableConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.utils.PagingUtil;
import com.restaurant.crm.modules.erp.organization.constants.BranchManagerConstants;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerCreationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerUpdateRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgPermission;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.BranchManagerMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgPermissionRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.interfaces.BranchManagerService;
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
                .orgRole(getOrCreateBranchManagerRole())
                .email(request.getEmail())
                .phone(request.getPhone())
                .startDate(LocalDate.now())
                .status(EmployeeStatus.ACTIVE)
                .build();
        Employee savedEmployee = employeeRepository.save(employee);

        branch.setManager(savedEmployee);
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
                ? employeeRepository.findAllByOrgRole_RoleNameAndBranch_IdAndBranch_Organization_Owner_Id(
                        BranchManagerConstants.BRANCH_MANAGER_ROLE,
                        branchId,
                        ownerId,
                        pageable)
                : employeeRepository.findAllByOrgRole_RoleNameAndBranch_Organization_Owner_Id(
                        BranchManagerConstants.BRANCH_MANAGER_ROLE,
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
        if (branch.getManager() != null && employee.getId().equals(branch.getManager().getId())) {
            branch.setManager(null);
            branchRepository.save(branch);
        }

        userRepository.save(employee.getUser());
        employeeRepository.save(employee);
    }

    private Employee findBranchManager(String branchManagerId, String ownerId) {
        return employeeRepository.findByIdAndOrgRole_RoleNameAndBranch_Organization_Owner_Id(
                        branchManagerId,
                        BranchManagerConstants.BRANCH_MANAGER_ROLE,
                        ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_MANAGER_NOT_FOUND));
    }

    private OrganizationBranch findBranch(String branchId, String ownerId) {
        return branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId)
                .orElseThrow(() -> new AppException(ErrorCode.BRANCH_NOT_FOUND));
    }

    private OrgRole getOrCreateBranchManagerRole() {
        return orgRoleRepository.findByRoleName(BranchManagerConstants.BRANCH_MANAGER_ROLE)
                .orElseGet(() -> orgRoleRepository.save(OrgRole.builder()
                        .roleName(BranchManagerConstants.BRANCH_MANAGER_ROLE)
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
            branch.setManager(employee);
        } else if (branch.getManager() != null && employee.getId().equals(branch.getManager().getId())) {
            branch.setManager(null);
        }
        branchRepository.save(branch);
    }

    private void ensureBranchHasNoOtherActiveManager(String branchId, String currentEmployeeId) {
        boolean existed = currentEmployeeId == null
                ? employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatus(
                        branchId,
                        BranchManagerConstants.BRANCH_MANAGER_ROLE,
                        EmployeeStatus.ACTIVE)
                : employeeRepository.existsByBranch_IdAndOrgRole_RoleNameAndStatusAndIdNot(
                        branchId,
                        BranchManagerConstants.BRANCH_MANAGER_ROLE,
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
