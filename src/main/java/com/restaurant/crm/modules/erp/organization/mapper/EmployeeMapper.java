package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    default EmployeeBranchAssignmentResponse toEmployeeBranchAssignmentResponse(
            OrganizationBranch branch,
            Employee manager
    ) {
        return EmployeeBranchAssignmentResponse.builder()
                .employeeId(manager.getId())
                .managerId(manager.getId())
                .userId(manager.getUser() == null ? null : manager.getUser().getId())
                .managerUserId(manager.getUser() == null ? null : manager.getUser().getId())
                .username(manager.getUser() == null ? null : manager.getUser().getUsername())
                .managerName(manager.getUser() == null ? null : manager.getUser().getUsername())
                .email(manager.getUser() == null ? null : manager.getUser().getEmail())
                .enabled(manager.getUser() != null && manager.getUser().isEnabled())
                .phone(manager.getPhone())
                .status(manager.getStatus())
                .startDate(manager.getStartDate())
                .endDate(manager.getEndDate())
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getAddress())
                .branchPhone(branch.getPhone())
                .branchStatus(branch.getStatus() == null ? null : branch.getStatus().name())
                .orgRoleId(manager.getOrgRole() == null ? null : manager.getOrgRole().getId())
                .orgRoleName(manager.getOrgRole() == null ? null : manager.getOrgRole().getRoleName())
                .role(manager.getOrgRole() == null ? null : manager.getOrgRole().getRoleName())
                .build();
    }

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "organizationId", source = "branch.organization.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "orgRoleId", source = "orgRole.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    @Mapping(target = "role", source = "orgRole.roleName")
    @Mapping(target = "enabled", source = "user.enabled")
    @Mapping(target = "userStatus", source = "user.status")
    EmployeeResponse toEmployeeResponse(Employee employee);
}
