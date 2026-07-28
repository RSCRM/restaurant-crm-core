package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrgRoleRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.impl.EmployeeServiceImpl;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.repository.RoleRepository;
import com.restaurant.crm.modules.identity.repository.UserRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
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
    PasswordEncoder passwordEncoder;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Test
    void assignToBranch_success_usesEmployeeIdAndDoesNotMoveEmployee() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        EmployeeBranchAssignmentResponse expectedResponse = response(targetBranch, manager);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(manager.getId()))
                    .thenReturn(Optional.of(manager));
            when(branchRepository.findByManager_Id(manager.getId())).thenReturn(Optional.empty());
            when(branchRepository.saveAndFlush(targetBranch)).thenReturn(targetBranch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(manager.getId()).build()
            );

            assertSame(manager, targetBranch.getManager());
            assertSame(targetBranch, manager.getBranch());
            assertEquals(manager.getId(), response.getEmployeeId());
            verify(branchRepository).saveAndFlush(targetBranch);
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void assignToBranch_employeeNotFound_throwsNotFound() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch("missing-employee"))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId("missing-employee").build()
            ));

            assertEquals(ErrorCode.BRANCH_MANAGER_NOT_FOUND, ex.getErrorCode());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    @Test
    void assignToBranch_employeeBelongsToAnotherBranch_throwsInvalidBranch() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        OrganizationBranch otherBranch = buildBranch("branch-2", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", otherBranch, "MANAGER");

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH, ex.getErrorCode());
        verify(branchRepository, never()).findByManager_Id(manager.getId());
        verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
    }

    @Test
    void assignToBranch_employeeRoleCashier_throwsInvalidRole() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "CASHIER");

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_ROLE, ex.getErrorCode());
    }

    @Test
    void assignToBranch_inactiveEmployee_throwsInactive() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        manager.setStatus(EmployeeStatus.INACTIVE);

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
    }

    @Test
    void assignToBranch_disabledUser_throwsInactive() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        manager.getUser().setEnabled(false);

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
    }

    @Test
    void assignToBranch_blockedUser_throwsInactive() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        manager.getUser().setStatus(UserStatus.BLOCKED);

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
    }

    @Test
    void assignToBranch_expiredEmployee_throwsExpired() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        manager.setEndDate(LocalDate.now().minusDays(1));

        AppException ex = assignExpectingException(ownerId, targetBranch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_EXPIRED, ex.getErrorCode());
    }

    @Test
    void assignToBranch_sameManager_returnsIdempotentlyWithoutSaving() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        targetBranch.setManager(manager);
        EmployeeBranchAssignmentResponse expectedResponse = response(targetBranch, manager);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(manager.getId()))
                    .thenReturn(Optional.of(manager));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(manager.getId()).build()
            );

            assertEquals(manager.getId(), response.getEmployeeId());
            verify(branchRepository, never()).findByManager_Id(manager.getId());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void assignToBranch_replacesCurrentManagerWithNewManager() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        Employee oldManager = buildEmployee("employee-old", "user-old", targetBranch, "MANAGER");
        Employee newManager = buildEmployee("employee-new", "user-new", targetBranch, "MANAGER");
        targetBranch.setManager(oldManager);
        EmployeeBranchAssignmentResponse expectedResponse = response(targetBranch, newManager);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(newManager.getId()))
                    .thenReturn(Optional.of(newManager));
            when(branchRepository.findByManager_Id(newManager.getId())).thenReturn(Optional.empty());
            when(branchRepository.saveAndFlush(targetBranch)).thenReturn(targetBranch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(newManager.getId()).build()
            );

            assertSame(newManager, targetBranch.getManager());
            assertEquals(newManager.getId(), response.getEmployeeId());
            verify(branchRepository).saveAndFlush(targetBranch);
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void assignToBranch_employeeManagingOtherBranch_unassignsOldBranchOnly() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);
        OrganizationBranch oldManagedBranch = buildBranch("branch-2", "org-1", ownerId);
        Employee manager = buildEmployee("employee-1", "user-manager-1", targetBranch, "MANAGER");
        oldManagedBranch.setManager(manager);
        EmployeeBranchAssignmentResponse expectedResponse = response(targetBranch, manager);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(manager.getId()))
                    .thenReturn(Optional.of(manager));
            when(branchRepository.findByManager_Id(manager.getId())).thenReturn(Optional.of(oldManagedBranch));
            when(branchRepository.saveAndFlush(oldManagedBranch)).thenReturn(oldManagedBranch);
            when(branchRepository.saveAndFlush(targetBranch)).thenReturn(targetBranch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

            employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(manager.getId()).build()
            );

            assertNull(oldManagedBranch.getManager());
            assertSame(manager, targetBranch.getManager());
            assertSame(targetBranch, manager.getBranch());
            verify(branchRepository).saveAndFlush(oldManagedBranch);
            verify(branchRepository).saveAndFlush(targetBranch);
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    void assignToBranch_ownerDoesNotOwnBranch_throwsBranchNotFound() {
        String ownerId = "owner-1";

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager("branch-1", ownerId))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(
                    "branch-1",
                    EmployeeBranchAssignmentRequest.builder().managerId("employee-1").build()
            ));

            assertEquals(ErrorCode.BRANCH_NOT_FOUND, ex.getErrorCode());
            verify(employeeRepository, never()).findByIdWithUserRoleAndBranch(any());
        }
    }

    @Test
    void assignToBranch_nullManagerId_throwsInvalidRequest() {
        String ownerId = "owner-1";
        OrganizationBranch targetBranch = buildBranch("branch-1", "org-1", ownerId);

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(null).build()
            ));

            assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST, ex.getErrorCode());
            verify(employeeRepository, never()).findByIdWithUserRoleAndBranch(any());
        }
    }

    @Test
    void assignToBranch_unauthenticated_throwsUnauthenticated() {
        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(null);

            AppException ex = assertThrows(AppException.class, () -> employeeService.assignToBranch(
                    "branch-1",
                    EmployeeBranchAssignmentRequest.builder().managerId("employee-1").build()
            ));

            assertEquals(ErrorCode.AUTH_UNAUTHENTICATED, ex.getErrorCode());
            verify(branchRepository, never()).findByIdAndOwnerIdWithManager(any(), any());
        }
    }

    private AppException assignExpectingException(
            String ownerId,
            OrganizationBranch targetBranch,
            Employee manager
    ) {
        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOwnerIdWithManager(targetBranch.getId(), ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(manager.getId()))
                    .thenReturn(Optional.of(manager));

            return assertThrows(AppException.class, () -> employeeService.assignToBranch(
                    targetBranch.getId(),
                    EmployeeBranchAssignmentRequest.builder().managerId(manager.getId()).build()
            ));
        }
    }

    private EmployeeBranchAssignmentResponse response(OrganizationBranch branch, Employee manager) {
        return EmployeeBranchAssignmentResponse.builder()
                .employeeId(manager.getId())
                .managerId(manager.getId())
                .userId(manager.getUser().getId())
                .managerUserId(manager.getUser().getId())
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .orgRoleName(manager.getOrgRole().getRoleName())
                .role(manager.getOrgRole().getRoleName())
                .status(manager.getStatus())
                .build();
    }

    private OrganizationBranch buildBranch(String branchId, String organizationId, String ownerId) {
        User owner = User.builder()
                .id(ownerId)
                .build();
        Organization organization = Organization.builder()
                .id(organizationId)
                .owner(owner)
                .build();

        return OrganizationBranch.builder()
                .id(branchId)
                .organization(organization)
                .branchName("Main Branch")
                .build();
    }

    private Employee buildEmployee(
            String employeeId,
            String userId,
            OrganizationBranch branch,
            String roleName
    ) {
        User user = User.builder()
                .id(userId)
                .username("manager")
                .email("manager@example.com")
                .status(UserStatus.ACTIVE)
                .enabled(true)
                .build();
        OrgRole orgRole = OrgRole.builder()
                .id("role-" + roleName.toLowerCase())
                .roleName(roleName)
                .build();

        return Employee.builder()
                .id(employeeId)
                .user(user)
                .orgRole(orgRole)
                .branch(branch)
                .status(EmployeeStatus.ACTIVE)
                .email("manager@example.com")
                .phone("0904000001")
                .startDate(LocalDate.now().minusYears(1))
                .build();
    }
}
