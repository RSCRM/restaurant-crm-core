package com.restaurant.crm.modules.erp.manage_branch_manager.mapper;

import com.restaurant.crm.modules.erp.manage_branch_manager.dto.response.BranchManagerResponse;
import com.restaurant.crm.modules.erp.shared.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface BranchManagerMapper {

    @Mapping(target = "employeeId", source = "id")
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "email", source = "user.email")
    @Mapping(target = "enabled", source = "user.enabled")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "branchName", source = "branch.branchName")
    @Mapping(target = "orgRoleId", source = "orgRole.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    BranchManagerResponse toBranchManagerResponse(Employee employee);
}
