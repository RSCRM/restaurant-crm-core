package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgRole;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.impl.EmployeeServiceImpl;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.enums.UserStatus;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

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
public class EmployeeServiceImplTests {

    private static final String OWNER_ID = "owner-id";
    private static final String BRANCH_ID = "branch-id";
    private static final String OTHER_BRANCH_ID = "other-branch-id";
    private static final String MANAGER_EMPLOYEE_ID = "manager-employee-id";
    private static final String OLD_MANAGER_EMPLOYEE_ID = "old-manager-employee-id";

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    OrganizationBranchRepository branchRepository;
    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Test
    public void assignToBranch_Success_UsesEmployeeIdAndSavesOnlyBranch() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        EmployeeBranchAssignmentResponse response = response(BRANCH_ID, MANAGER_EMPLOYEE_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(manager));
            when(branchRepository.findByManager_Id(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.empty());
            when(branchRepository.saveAndFlush(branch)).thenReturn(branch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch)).thenReturn(response);

            EmployeeBranchAssignmentResponse result =
                    employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID));

            assertSame(response, result);
            assertSame(manager, branch.getManager());
            verify(employeeRepository).findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID);
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(branchRepository).saveAndFlush(branch);
        }
    }

    @Test
    public void assignToBranch_ManagerIdNotFound_ThrowsNotFound() {
        OrganizationBranch branch = branch(BRANCH_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(
                    AppException.class,
                    () -> employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID))
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_NOT_FOUND, ex.getErrorCode());
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    @Test
    public void assignToBranch_EmployeeFromDifferentBranch_ThrowsInvalidBranch() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch(OTHER_BRANCH_ID));

        AppException ex = assignFailure(branch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_BRANCH, ex.getErrorCode());
    }

    @Test
    public void assignToBranch_EmployeeNotManagerRole_ThrowsInvalidRole() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        manager.setOrgRole(role(StartDefinedOrgRole.CASHIER));

        AppException ex = assignFailure(branch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_ROLE, ex.getErrorCode());
    }

    @Test
    public void assignToBranch_InactiveEmployee_ThrowsInactive() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        manager.setStatus(EmployeeStatus.INACTIVE);

        AppException ex = assignFailure(branch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
    }

    @Test
    public void assignToBranch_DisabledUser_ThrowsInactive() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        manager.getUser().setEnabled(false);

        AppException ex = assignFailure(branch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
    }

    @Test
    public void assignToBranch_ExpiredEmployee_ThrowsExpired() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        manager.setEndDate(LocalDate.now().minusDays(1));

        AppException ex = assignFailure(branch, manager);

        assertEquals(ErrorCode.BRANCH_MANAGER_EXPIRED, ex.getErrorCode());
    }

    @Test
    public void assignToBranch_SameManager_ReturnsCurrentAssignmentWithoutSaving() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        branch.setManager(manager);
        EmployeeBranchAssignmentResponse response = response(BRANCH_ID, MANAGER_EMPLOYEE_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(manager));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch)).thenReturn(response);

            EmployeeBranchAssignmentResponse result =
                    employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID));

            assertSame(response, result);
            verify(branchRepository, never()).findByManager_Id(MANAGER_EMPLOYEE_ID);
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void assignToBranch_ReplacesOldManagerOnBranch() {
        OrganizationBranch branch = branch(BRANCH_ID);
        Employee oldManager = managerEmployee(OLD_MANAGER_EMPLOYEE_ID, branch);
        Employee newManager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        branch.setManager(oldManager);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(newManager));
            when(branchRepository.findByManager_Id(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.empty());
            when(branchRepository.saveAndFlush(branch)).thenReturn(branch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch))
                    .thenReturn(response(BRANCH_ID, MANAGER_EMPLOYEE_ID));

            employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID));

            assertSame(newManager, branch.getManager());
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void assignToBranch_EmployeeManagingOtherBranch_ClearsOtherBranchReference() {
        OrganizationBranch branch = branch(BRANCH_ID);
        OrganizationBranch oldManagedBranch = branch(OTHER_BRANCH_ID);
        Employee manager = managerEmployee(MANAGER_EMPLOYEE_ID, branch);
        oldManagedBranch.setManager(manager);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(manager));
            when(branchRepository.findByManager_Id(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(oldManagedBranch));
            when(branchRepository.saveAndFlush(oldManagedBranch)).thenReturn(oldManagedBranch);
            when(branchRepository.saveAndFlush(branch)).thenReturn(branch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch))
                    .thenReturn(response(BRANCH_ID, MANAGER_EMPLOYEE_ID));

            employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID));

            assertNull(oldManagedBranch.getManager());
            assertSame(manager, branch.getManager());
            verify(branchRepository).saveAndFlush(oldManagedBranch);
            verify(branchRepository).saveAndFlush(branch);
        }
    }

    @Test
    public void removeManager_Success_ClearsBranchManager() {
        OrganizationBranch branch = branch(BRANCH_ID);
        branch.setManager(managerEmployee(MANAGER_EMPLOYEE_ID, branch));
        EmployeeBranchAssignmentResponse response = response(BRANCH_ID, null);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(branchRepository.saveAndFlush(branch)).thenReturn(branch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch)).thenReturn(response);

            EmployeeBranchAssignmentResponse result = employeeService.removeManager(BRANCH_ID);

            assertSame(response, result);
            assertNull(branch.getManager());
            verify(employeeRepository, never()).save(any(Employee.class));
            verify(branchRepository).saveAndFlush(branch);
        }
    }

    @Test
    public void removeManager_WhenNoManager_ReturnsSuccessWithoutSaving() {
        OrganizationBranch branch = branch(BRANCH_ID);
        EmployeeBranchAssignmentResponse response = response(BRANCH_ID, null);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch)).thenReturn(response);

            EmployeeBranchAssignmentResponse result = employeeService.removeManager(BRANCH_ID);

            assertSame(response, result);
            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
        }
    }

    @Test
    public void getBranchManager_Success_ReturnsCurrentManager() {
        OrganizationBranch branch = branch(BRANCH_ID);
        branch.setManager(managerEmployee(MANAGER_EMPLOYEE_ID, branch));
        EmployeeBranchAssignmentResponse response = response(BRANCH_ID, MANAGER_EMPLOYEE_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(branch)).thenReturn(response);

            EmployeeBranchAssignmentResponse result = employeeService.getBranchManager(BRANCH_ID);

            assertSame(response, result);
        }
    }

    @Test
    public void getBranchManager_WhenOwnerDoesNotOwnBranch_ThrowsBranchNotFound() {
        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(
                    AppException.class,
                    () -> employeeService.getBranchManager(BRANCH_ID)
            );

            assertEquals(ErrorCode.BRANCH_NOT_FOUND, ex.getErrorCode());
        }
    }

    @Test
    public void assignToBranch_NullManagerId_ThrowsInvalidRequest() {
        OrganizationBranch branch = branch(BRANCH_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));

            AppException ex = assertThrows(
                    AppException.class,
                    () -> employeeService.assignToBranch(BRANCH_ID, request(null))
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST, ex.getErrorCode());
            verify(employeeRepository, never()).findByIdWithUserRoleAndBranch(any());
        }
    }

    @Test
    public void assignToBranch_BlankManagerId_ThrowsInvalidRequest() {
        OrganizationBranch branch = branch(BRANCH_ID);

        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));

            AppException ex = assertThrows(
                    AppException.class,
                    () -> employeeService.assignToBranch(BRANCH_ID, request("   "))
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_INVALID_REQUEST, ex.getErrorCode());
            verify(employeeRepository, never()).findByIdWithUserRoleAndBranch(any());
        }
    }

    private AppException assignFailure(OrganizationBranch branch, Employee manager) {
        try (MockedStatic<AuthUtils> auth = mockCurrentOwner()) {
            when(branchRepository.findByIdAndOwnerIdWithManager(BRANCH_ID, OWNER_ID))
                    .thenReturn(Optional.of(branch));
            when(employeeRepository.findByIdWithUserRoleAndBranch(MANAGER_EMPLOYEE_ID))
                    .thenReturn(Optional.of(manager));

            AppException ex = assertThrows(
                    AppException.class,
                    () -> employeeService.assignToBranch(BRANCH_ID, request(MANAGER_EMPLOYEE_ID))
            );

            verify(branchRepository, never()).saveAndFlush(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
            return ex;
        }
    }

    private MockedStatic<AuthUtils> mockCurrentOwner() {
        MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getCurrentUserId).thenReturn(OWNER_ID);
        return auth;
    }

    private EmployeeBranchAssignmentRequest request(String managerId) {
        EmployeeBranchAssignmentRequest request = new EmployeeBranchAssignmentRequest();
        request.setManagerId(managerId);
        return request;
    }

    private EmployeeBranchAssignmentResponse response(String branchId, String managerId) {
        return EmployeeBranchAssignmentResponse.builder()
                .branchId(branchId)
                .managerId(managerId)
                .employeeId(managerId)
                .build();
    }

    private OrganizationBranch branch(String branchId) {
        Organization organization = Organization.builder()
                .id("organization-id")
                .owner(User.builder().id(OWNER_ID).build())
                .organizationName("Restaurant Group")
                .build();

        return OrganizationBranch.builder()
                .id(branchId)
                .organization(organization)
                .branchName("Branch " + branchId)
                .build();
    }

    private Employee managerEmployee(String employeeId, OrganizationBranch branch) {
        return Employee.builder()
                .id(employeeId)
                .user(User.builder()
                        .id("user-" + employeeId)
                        .username("manager")
                        .email("manager@example.com")
                        .enabled(true)
                        .status(UserStatus.ACTIVE)
                        .build())
                .orgRole(role(StartDefinedOrgRole.MANAGER))
                .branch(branch)
                .status(EmployeeStatus.ACTIVE)
                .email("manager@example.com")
                .phone("0904000001")
                .startDate(LocalDate.now().minusDays(1))
                .build();
    }

    private OrgRole role(String roleName) {
        return OrgRole.builder()
                .id("role-" + roleName)
                .roleName(roleName)
                .build();
    }
}
