package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

    default EmployeeBranchAssignmentResponse toEmployeeBranchAssignmentResponse(OrganizationBranch branch) {
        if (branch == null) {
            return null;
        }

        EmployeeResponse manager = toEmployeeResponse(branch.getManager());

        return EmployeeBranchAssignmentResponse.builder()
                .branchId(branch.getId())
                .branchName(branch.getBranchName())
                .branchAddress(branch.getAddress())
                .branchPhone(branch.getPhone())
                .branchStatus(branch.getStatus() == null ? null : branch.getStatus().name())
                .managerId(manager == null ? null : manager.getId())
                .managerName(manager == null ? null : manager.getFullName())
                .manager(manager)
                .build();
    }

    default EmployeeResponse toEmployeeResponse(Employee employee) {
        if (employee == null) {
            return null;
        }

        OrganizationBranch branch = employee.getBranch();
        String firstName = employee.getFirstName();
        String lastName = employee.getLastName();
        String fullName = buildFullName(firstName, lastName);
        String username = employee.getUser() == null ? null : employee.getUser().getUsername();
        String roleName = employee.getOrgRole() == null ? null : employee.getOrgRole().getRoleName();

        return EmployeeResponse.builder()
                .id(employee.getId())
                .employeeId(employee.getId())
                .employeeCode(employee.getId())
                .firstName(firstName)
                .lastName(lastName)
                .fullName(fullName != null ? fullName : username)
                .userId(employee.getUser() == null ? null : employee.getUser().getId())
                .username(username)
                .email(employee.getEmail())
                .phone(employee.getPhone())
                .organizationId(branch == null || branch.getOrganization() == null ? null : branch.getOrganization().getId())
                .branchId(branch == null ? null : branch.getId())
                .branchName(branch == null ? null : branch.getBranchName())
                .orgRoleId(employee.getOrgRole() == null ? null : employee.getOrgRole().getId())
                .orgRoleName(roleName)
                .role(roleName)
                .status(employee.getStatus())
                .enabled(employee.getUser() != null && employee.getUser().isEnabled())
                .userStatus(employee.getUser() == null ? null : employee.getUser().getStatus())
                .startDate(employee.getStartDate())
                .endDate(employee.getEndDate())
                .createdAt(employee.getCreatedAt())
                .updatedAt(employee.getUpdatedAt())
                .build();
    }

    private String buildFullName(String firstName, String lastName) {
        String fullName = ((firstName == null ? "" : firstName.trim())
                + " "
                + (lastName == null ? "" : lastName.trim())).trim();
        return fullName.isBlank() ? null : fullName;
    }
}
