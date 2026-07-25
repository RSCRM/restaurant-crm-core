package com.restaurant.crm.modules.erp.employee.mapper;

import com.restaurant.crm.modules.erp.employee.dto.response.EmployeeResponse;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface EmployeeMapper {

    @Mapping(target = "username", source = "user.username")
    @Mapping(target = "branchId", source = "branch.id")
    @Mapping(target = "orgRoleName", source = "orgRole.roleName")
    EmployeeResponse toEmployeeResponse(Employee employee);
}
