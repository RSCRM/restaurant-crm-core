package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface EmployeeMapper {

//    @Mapping(target = "employeeId", source = "id")
//    @Mapping(target = "userId", source = "user.id")
//    @Mapping(target = "username", source = "user.username")
//    @Mapping(target = "email", source = "user.email")
//    @Mapping(target = "enabled", source = "user.enabled")
//    @Mapping(target = "organizationId", source = "branch.organization.id")
//    @Mapping(target = "branchId", source = "branch.id")
//    @Mapping(target = "branchName", source = "branch.branchName")
//    @Mapping(target = "orgRoleId", source = "orgRole.id")
//    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
//    EmployeeBranchAssignmentResponse toEmployeeBranchAssignmentResponse(Employee employee);

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    EmployeeResponse toEmployeeResponse(Employee employee);
}
