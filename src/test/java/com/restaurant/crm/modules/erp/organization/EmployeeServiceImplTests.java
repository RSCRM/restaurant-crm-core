package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrgDataScope;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.impl.EmployeeServiceImpl;
import com.restaurant.crm.modules.identity.constants.role.PredefinedRole;
import com.restaurant.crm.modules.identity.entity.Role;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTests {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    OrganizationBranchRepository branchRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @Mock
    UserRepository userRepository;

    @Mock
    RoleRepository roleRepository;

    @Mock
    OrgRoleRepository orgRoleRepository;

    @Mock
    UserProfileRepository userProfileRepository;

    @Mock
    PasswordEncoder passwordEncoder;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Test
    void addEmployee_organizationContextCreatesUserProfileAndEmployee() {
        String currentUserId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String orgRoleId = "role-manager";
        OrganizationBranch branch = buildBranch(branchId, organizationId, currentUserId, null);
        OrgRole orgRole = OrgRole.builder().id(orgRoleId).roleName(EmployeeConstants.MANAGER_ROLE_NAME).build();
        Role systemRole = Role.builder().id("system-user").roleName(PredefinedRole.USER_ROLE).build();
        CreateEmployeeRequest request = buildCreateRequest(branchId, orgRoleId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(currentUserId);
            mockedAuth.when(() -> AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)).thenReturn(false);
            mockedAuth.when(AuthUtils::getDataScope).thenReturn(OrgDataScope.ORGANIZATION);
            mockedAuth.when(AuthUtils::getOrganizationId).thenReturn(organizationId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, currentUserId)).thenReturn(Optional.empty());
            when(branchRepository.findById(branchId)).thenReturn(Optional.of(branch));
            when(orgRoleRepository.findById(orgRoleId)).thenReturn(Optional.of(orgRole));
            when(userProfileRepository.existsByPhone("0909000000")).thenReturn(false);
            when(userRepository.existsByUsername("new.employee")).thenReturn(false);
            when(userRepository.existsByEmail("employee@example.com")).thenReturn(false);
            when(roleRepository.findByRoleName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(systemRole));
            when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
                User user = invocation.getArgument(0);
                user.setId("created-user");
                return user;
            });
            when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> {
                UserProfile profile = invocation.getArgument(0);
                profile.setId("created-profile");
                return profile;
            });
            when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> {
                Employee employee = invocation.getArgument(0);
                employee.setId("created-employee");
                return employee;
            });
            when(employeeMapper.toEmployeeResponse(any(Employee.class))).thenAnswer(invocation -> {
                Employee employee = invocation.getArgument(0);
                return EmployeeResponse.builder()
                        .id(employee.getId())
                        .userId(employee.getUser().getId())
                        .branchId(employee.getBranch().getId())
                        .orgRoleId(employee.getOrgRole().getId())
                        .email(employee.getEmail())
                        .phone(employee.getPhone())
                        .status(employee.getStatus().name())
                        .enabled(employee.getUser().isEnabled())
                        .build();
            });

            EmployeeResponse response = employeeService.addEmployee(request);

            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(userRepository).save(userCaptor.capture());
            verify(userProfileRepository).save(profileCaptor.capture());
            verify(employeeRepository).save(employeeCaptor.capture());

            User savedUser = userCaptor.getValue();
            UserProfile savedProfile = profileCaptor.getValue();
            Employee savedEmployee = employeeCaptor.getValue();

            assertEquals("new.employee", savedUser.getUsername());
            assertEquals("employee@example.com", savedUser.getEmail());
            assertEquals("encoded-password", savedUser.getPassword());
            assertNotEquals("Password@123", savedUser.getPassword());
            assertTrue(savedUser.isEnabled());
            assertEquals(UserStatus.ACTIVE, savedUser.getStatus());
            assertTrue(savedUser.getRoles().contains(systemRole));

            assertSame(savedUser, savedProfile.getUser());
            assertEquals("New Employee", savedProfile.getFullName());
            assertEquals("0909000000", savedProfile.getPhone());

            assertSame(savedUser, savedEmployee.getUser());
            assertSame(branch, savedEmployee.getBranch());
            assertSame(orgRole, savedEmployee.getOrgRole());
            assertEquals("New", savedEmployee.getFirstName());
            assertEquals("Employee", savedEmployee.getLastName());
            assertEquals("employee@example.com", savedEmployee.getEmail());
            assertEquals("0909000000", savedEmployee.getPhone());
            assertEquals(EmployeeStatus.ACTIVE, savedEmployee.getStatus());
            assertEquals(new BigDecimal("12000000"), savedEmployee.getSalary());
            assertEquals("New Employee", response.getFullName());
            assertEquals("New", response.getFirstName());
            assertEquals("Employee", response.getLastName());
        }
    }

    @Test
    void addEmployee_branchContextUsesContextBranchWhenRequestBranchMissing() {
        String currentUserId = "manager-user";
        String organizationId = "org-1";
        String contextBranchId = "branch-context";
        String orgRoleId = "role-manager";
        OrganizationBranch branch = buildBranch(contextBranchId, organizationId, "owner-1", null);
        CreateEmployeeRequest request = buildCreateRequest(null, orgRoleId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getDataScope).thenReturn(OrgDataScope.BRANCH);
            mockedAuth.when(AuthUtils::getBranchId).thenReturn(contextBranchId);
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(currentUserId);
            mockedAuth.when(() -> AuthUtils.hasRole(PredefinedRole.ADMIN_ROLE)).thenReturn(false);
            when(branchRepository.findByIdAndOwnerIdWithManager(contextBranchId, currentUserId)).thenReturn(Optional.empty());
            when(branchRepository.findById(contextBranchId)).thenReturn(Optional.of(branch));
            stubCreateDependencies(orgRoleId);

            employeeService.addEmployee(request);

            ArgumentCaptor<Employee> employeeCaptor = ArgumentCaptor.forClass(Employee.class);
            verify(employeeRepository).save(employeeCaptor.capture());
            assertSame(branch, employeeCaptor.getValue().getBranch());
        }
    }

    @Test
    void addEmployee_branchContextRejectsDifferentBranch() {
        CreateEmployeeRequest request = buildCreateRequest("other-branch", "role-manager");

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getDataScope).thenReturn(OrgDataScope.BRANCH);
            mockedAuth.when(AuthUtils::getBranchId).thenReturn("branch-context");

            AppException ex = assertThrows(AppException.class, () -> employeeService.addEmployee(request));

            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void addEmployee_duplicateUsernameDoesNotCreateUserOrEmployee() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String orgRoleId = "role-manager";
        OrganizationBranch branch = buildBranch(branchId, organizationId, ownerId, null);
        CreateEmployeeRequest request = buildCreateRequest(branchId, orgRoleId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId)).thenReturn(Optional.of(branch));
            when(orgRoleRepository.findById(orgRoleId)).thenReturn(Optional.of(OrgRole.builder().id(orgRoleId).build()));
            when(userProfileRepository.existsByPhone("0909000000")).thenReturn(false);
            when(userRepository.existsByUsername("new.employee")).thenReturn(true);

            AppException ex = assertThrows(AppException.class, () -> employeeService.addEmployee(request));

            assertEquals(ErrorCode.USER_USERNAME_ALREADY_EXISTS, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void addEmployee_duplicateEmailDoesNotCreateUserOrEmployee() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String orgRoleId = "role-manager";
        OrganizationBranch branch = buildBranch(branchId, organizationId, ownerId, null);
        CreateEmployeeRequest request = buildCreateRequest(branchId, orgRoleId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId)).thenReturn(Optional.of(branch));
            when(orgRoleRepository.findById(orgRoleId)).thenReturn(Optional.of(OrgRole.builder().id(orgRoleId).build()));
            when(userProfileRepository.existsByPhone("0909000000")).thenReturn(false);
            when(userRepository.existsByUsername("new.employee")).thenReturn(false);
            when(userRepository.existsByEmail("employee@example.com")).thenReturn(true);

            AppException ex = assertThrows(AppException.class, () -> employeeService.addEmployee(request));

            assertEquals(ErrorCode.EMAIL_ALREADY_EXISTS, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void addEmployee_blankPasswordDoesNotCreateUserOrEmployee() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String orgRoleId = "role-manager";
        OrganizationBranch branch = buildBranch(branchId, organizationId, ownerId, null);
        CreateEmployeeRequest request = buildCreateRequest(branchId, orgRoleId);
        request.setPassword(" ");

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId)).thenReturn(Optional.of(branch));
            when(orgRoleRepository.findById(orgRoleId)).thenReturn(Optional.of(OrgRole.builder().id(orgRoleId).build()));
            when(userProfileRepository.existsByPhone("0909000000")).thenReturn(false);

            AppException ex = assertThrows(AppException.class, () -> employeeService.addEmployee(request));

            assertEquals(ErrorCode.USER_PASSWORD_INVALID, ex.getErrorCode());
            verify(userRepository, never()).save(any(User.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void assignToBranch_resolvesManagerIdAsUserId() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerUserId = "user-manager-1";

        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee("employee-1", managerUserId, targetBranch, EmployeeStatus.ACTIVE, true,
                EmployeeConstants.MANAGER_ROLE_NAME);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerUserId)
                .build();
        EmployeeBranchAssignmentResponse expectedResponse = EmployeeBranchAssignmentResponse.builder()
                .employeeId("employee-1")
                .userId(managerUserId)
                .branchId(branchId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(List.of(manager));
            when(branchRepository.findByManagerId(manager.getId())).thenReturn(List.of());
            when(branchRepository.saveAndFlush(targetBranch)).thenReturn(targetBranch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch, manager)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(branchId, request);

            assertEquals(managerUserId, response.getUserId());
            assertEquals(manager.getId(), targetBranch.getManagerId());
            verify(employeeRepository, never()).findByIdWithUserRoleAndBranch(any());
            verify(branchRepository).saveAndFlush(targetBranch);
        }
    }

    @Test
    void assignToBranch_sameManagerIsIdempotent() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerUserId = "user-manager-1";

        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee("employee-1", managerUserId, targetBranch, EmployeeStatus.ACTIVE, true,
                EmployeeConstants.MANAGER_ROLE_NAME);
        targetBranch.setManagerId(manager.getId());
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerUserId)
                .build();
        EmployeeBranchAssignmentResponse expectedResponse = EmployeeBranchAssignmentResponse.builder()
                .employeeId("employee-1")
                .userId(managerUserId)
                .branchId(branchId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(List.of(manager));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch, manager)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(branchId, request);

            assertEquals(managerUserId, response.getUserId());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    @Test
    void assignToBranch_missingUserScopedEmployeeThrowsNotFound() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerUserId = "missing-user";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerUserId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(List.of());

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(branchId, request));

            assertEquals(ErrorCode.BRANCH_MANAGER_NOT_FOUND, ex.getErrorCode());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    @Test
    void assignToBranch_employeeWithoutManagerRoleThrowsInvalidRole() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerUserId = "user-manager-1";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee("employee-1", managerUserId, targetBranch, EmployeeStatus.ACTIVE, true,
                "CASHIER");
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerUserId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findAllByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(List.of(manager));

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(branchId, request));

            assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_ROLE, ex.getErrorCode());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    private OrganizationBranch buildBranch(String branchId, String organizationId, String ownerId, Employee manager) {
        Organization organization = Organization.builder()
                .id(organizationId)
                .owner(User.builder().id(ownerId).build())
                .build();

        return OrganizationBranch.builder()
                .id(branchId)
                .organization(organization)
                .managerId(manager == null ? null : manager.getId())
                .branchName("Main Branch")
                .build();
    }

    private CreateEmployeeRequest buildCreateRequest(String branchId, String orgRoleId) {
        return CreateEmployeeRequest.builder()
                .firstName(" New ")
                .lastName(" Employee ")
                .username(" new.employee ")
                .password("Password@123")
                .email(" employee@example.com ")
                .phone(" 0909000000 ")
                .branchId(branchId)
                .orgRoleId(orgRoleId)
                .status(EmployeeStatus.ACTIVE)
                .startDate(LocalDate.of(2026, 8, 4))
                .salary(new BigDecimal("12000000"))
                .build();
    }

    private void stubCreateDependencies(String orgRoleId) {
        when(orgRoleRepository.findById(orgRoleId)).thenReturn(Optional.of(
                OrgRole.builder().id(orgRoleId).roleName(EmployeeConstants.MANAGER_ROLE_NAME).build()));
        when(userProfileRepository.existsByPhone("0909000000")).thenReturn(false);
        when(userRepository.existsByUsername("new.employee")).thenReturn(false);
        when(userRepository.existsByEmail("employee@example.com")).thenReturn(false);
        when(roleRepository.findByRoleName(PredefinedRole.USER_ROLE)).thenReturn(Optional.of(
                Role.builder().id("system-user").roleName(PredefinedRole.USER_ROLE).build()));
        when(passwordEncoder.encode("Password@123")).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User user = invocation.getArgument(0);
            user.setId("created-user");
            return user;
        });
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeRepository.save(any(Employee.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(employeeMapper.toEmployeeResponse(any(Employee.class))).thenReturn(EmployeeResponse.builder().build());
    }

    private Employee buildEmployee(
            String employeeId,
            String userId,
            OrganizationBranch branch,
            EmployeeStatus status,
            boolean enabled,
            String roleName
    ) {
        User user = User.builder()
                .id(userId)
                .username("manager")
                .email("manager@example.com")
                .enabled(enabled)
                .status(enabled ? UserStatus.ACTIVE : UserStatus.BLOCKED)
                .build();

        OrgRole orgRole = OrgRole.builder()
                .id("role-" + roleName)
                .roleName(roleName)
                .build();

        return Employee.builder()
                .id(employeeId)
                .user(user)
                .branch(branch)
                .orgRole(orgRole)
                .status(status)
                .email("manager@example.com")
                .startDate(LocalDate.now())
                .build();
    }
}
