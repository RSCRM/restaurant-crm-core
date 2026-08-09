package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    List<EmployeeResponse> listEmployees();

    EmployeeResponse addEmployee(CreateEmployeeRequest request);

    EmployeeResponse assignRole(String employeeId, AssignRoleRequest request);

    EmployeeResponse revokeRole(String employeeId);

    EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request);

    EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request);
}
