package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.constants.EmployeeConstants;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrgRole;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
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

    @InjectMocks
    EmployeeServiceImpl employeeService;

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
            when(employeeRepository.findByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(Optional.of(manager));
            when(branchRepository.findByManager_Id(manager.getId())).thenReturn(Optional.empty());
            when(branchRepository.saveAndFlush(targetBranch)).thenReturn(targetBranch);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(branchId, request);

            assertEquals(managerUserId, response.getUserId());
            assertSame(manager, targetBranch.getManager());
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
        targetBranch.setManager(manager);
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
            when(employeeRepository.findByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(Optional.of(manager));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(targetBranch)).thenReturn(expectedResponse);

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
            when(employeeRepository.findByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(Optional.empty());

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
            when(employeeRepository.findByUserIdAndBranchIdAndOrganizationIdWithDetails(
                    managerUserId,
                    branchId,
                    organizationId
            )).thenReturn(Optional.of(manager));

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
                .manager(manager)
                .branchName("Main Branch")
                .build();
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
