package com.restaurant.crm.modules.erp.organization;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.mapper.EmployeeMapper;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.organization.service.impl.EmployeeServiceImpl;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class EmployeeServiceImplTests {

    @Mock
    EmployeeRepository employeeRepository;

    @Mock
    OrganizationBranchRepository branchRepository;

    @Mock
    EmployeeMapper employeeMapper;

    @InjectMocks
    EmployeeServiceImpl employeeService;

    @Test
    public void testAssignToBranch_Success() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "employee-1";
        String userId = "user-manager-1";

        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee(managerId, userId, targetBranch, EmployeeStatus.ACTIVE, true);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();
        EmployeeBranchAssignmentResponse expectedResponse = EmployeeBranchAssignmentResponse.builder()
                .employeeId(managerId)
                .userId(userId)
                .branchId(branchId)
                .status(EmployeeStatus.ACTIVE)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.of(manager));
            when(employeeRepository.save(manager)).thenReturn(manager);
            when(employeeMapper.toEmployeeBranchAssignmentResponse(manager)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(branchId, request);

            assertNotNull(response);
            assertEquals(managerId, targetBranch.getManager().getId());
            assertEquals(branchId, manager.getBranch().getId());
            assertEquals(userId, response.getUserId());
            verify(branchRepository, times(1)).save(targetBranch);
            verify(employeeRepository, times(1)).save(manager);
        }
    }

    @Test
    public void testAssignToBranch_ManagerAlreadyAssignedToSameBranch_ReturnsCurrentAssignment() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "employee-1";
        String userId = "user-manager-1";

        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, managerId);
        Employee manager = buildEmployee(managerId, userId, targetBranch, EmployeeStatus.ACTIVE, true);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();
        EmployeeBranchAssignmentResponse expectedResponse = EmployeeBranchAssignmentResponse.builder()
                .employeeId(managerId)
                .userId(userId)
                .branchId(branchId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.of(manager));
            when(employeeMapper.toEmployeeBranchAssignmentResponse(manager)).thenReturn(expectedResponse);

            EmployeeBranchAssignmentResponse response = employeeService.assignToBranch(branchId, request);

            assertEquals(userId, response.getUserId());
            verify(branchRepository, never()).save(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void testAssignToBranch_BranchNotFound_ThrowsAppException() {
        String ownerId = "owner-1";
        String branchId = "branch-not-found";
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId("user-manager-1")
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch(branchId, request)
            );

            assertEquals(ErrorCode.BRANCH_NOT_FOUND, ex.getErrorCode());
            verify(employeeRepository, never())
                    .findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(any(), any(), any());
        }
    }

    @Test
    public void testAssignToBranch_ManagerNotFound_ThrowsAppException() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "missing-user";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.empty());

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch(branchId, request)
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_NOT_FOUND, ex.getErrorCode());
            verify(branchRepository, never()).save(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void testAssignToBranch_InactiveEmployee_ThrowsAppException() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "employee-1";
        String userId = "user-manager-1";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee(managerId, userId, targetBranch, EmployeeStatus.INACTIVE, true);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.of(manager));

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch(branchId, request)
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
            verify(branchRepository, never()).save(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void testAssignToBranch_DisabledUser_ThrowsAppException() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "employee-1";
        String userId = "user-manager-1";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, null);
        Employee manager = buildEmployee(managerId, userId, targetBranch, EmployeeStatus.ACTIVE, false);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.of(manager));

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch(branchId, request)
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_INACTIVE, ex.getErrorCode());
            verify(branchRepository, never()).save(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void testAssignToBranch_BranchAlreadyHasAnotherManager_ThrowsAppException() {
        String ownerId = "owner-1";
        String organizationId = "org-1";
        String branchId = "branch-1";
        String managerId = "employee-1";
        String userId = "user-manager-1";
        OrganizationBranch targetBranch = buildBranch(branchId, organizationId, ownerId, "another-employee");
        Employee manager = buildEmployee(managerId, userId, targetBranch, EmployeeStatus.ACTIVE, true);
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId(managerId)
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(ownerId);
            when(branchRepository.findByIdAndOrganization_Owner_Id(branchId, ownerId))
                    .thenReturn(Optional.of(targetBranch));
            when(employeeRepository.findByIdAndBranch_Organization_IdAndBranch_Organization_Owner_Id(
                    managerId,
                    organizationId,
                    ownerId
            )).thenReturn(Optional.of(manager));

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch(branchId, request)
            );

            assertEquals(ErrorCode.BRANCH_MANAGER_ALREADY_ASSIGNED, ex.getErrorCode());
            verify(branchRepository, never()).save(any(OrganizationBranch.class));
            verify(employeeRepository, never()).save(any(Employee.class));
        }
    }

    @Test
    public void testAssignToBranch_Unauthenticated_ThrowsAppException() {
        EmployeeBranchAssignmentRequest request = EmployeeBranchAssignmentRequest.builder()
                .managerId("user-manager-1")
                .build();

        try (MockedStatic<AuthUtils> mockedAuth = mockStatic(AuthUtils.class)) {
            mockedAuth.when(AuthUtils::getCurrentUserId).thenReturn(null);

            AppException ex = assertThrows(AppException.class, () ->
                    employeeService.assignToBranch("branch-1", request)
            );

            assertEquals(ErrorCode.AUTH_UNAUTHENTICATED, ex.getErrorCode());
            verify(branchRepository, never()).findByIdAndOrganization_Owner_Id(any(), any());
        }
    }

    private OrganizationBranch buildBranch(String branchId, String organizationId, String ownerId, String managerId) {
        User owner = User.builder()
                .id(ownerId)
                .build();
        Organization organization = Organization.builder()
                .id(organizationId)
                .owner(owner)
                .build();
        Employee manager = managerId == null ? null : Employee.builder()
                .id(managerId)
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
            boolean enabled
    ) {
        User user = User.builder()
                .id(userId)
                .username("manager")
                .email("manager@example.com")
                .enabled(enabled)
                .build();

        return Employee.builder()
                .id(employeeId)
                .user(user)
                .branch(branch)
                .status(status)
                .email("manager@example.com")
                .build();
    }
}
