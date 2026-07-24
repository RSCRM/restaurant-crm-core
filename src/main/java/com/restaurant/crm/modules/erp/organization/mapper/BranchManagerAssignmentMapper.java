package com.restaurant.crm.modules.erp.organization.mapper;

import com.restaurant.crm.modules.erp.organization.dto.response.BranchManagerAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchManagerAssignmentMapper {

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "enabled", source = "user.enabled")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "orgRoleId", source = "orgRole.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    BranchManagerAssignmentResponse toBranchManagerAssignmentResponse(Employee employee);
}
