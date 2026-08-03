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

    @Mapping(target = "employeeId", source = "manager.id")
    @Mapping(target = "managerId", source = "manager.id")
    @Mapping(target = "userId", source = "manager.user.id")
    @Mapping(target = "managerUserId", source = "manager.user.id")
    @Mapping(target = "username", source = "manager.user.username")
    @Mapping(target = "managerName", source = "manager.user.username")
    @Mapping(target = "email", source = "manager.user.email")
    @Mapping(target = "enabled", source = "manager.user.enabled")
    @Mapping(target = "phone", source = "manager.phone")
    @Mapping(target = "status", source = "manager.status")
    @Mapping(target = "startDate", source = "manager.startDate")
    @Mapping(target = "endDate", source = "manager.endDate")
    @Mapping(target = "branchId", source = "id")
    @Mapping(target = "branchName", source = "branchName")
    @Mapping(target = "branchAddress", source = "address")
    @Mapping(target = "branchPhone", source = "phone")
    @Mapping(target = "branchStatus", source = "status")
    @Mapping(target = "orgRoleId", source = "manager.orgRole.id")
    @Mapping(target = "orgRoleName", source = "manager.orgRole.roleName")
    @Mapping(target = "role", source = "manager.orgRole.roleName")
    EmployeeBranchAssignmentResponse toEmployeeBranchAssignmentResponse(OrganizationBranch branch);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    EmployeeResponse toEmployeeResponse(Employee employee);
}
