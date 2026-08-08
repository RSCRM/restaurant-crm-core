package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;

import java.util.List;

public interface EmployeeService {

    EmployeeBranchAssignmentResponse getBranchManager(String branchId);

    EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request);

    EmployeeBranchAssignmentResponse removeBranchManager(String branchId);

    List<EmployeeResponse> listEmployees();

    EmployeeResponse addEmployee(CreateEmployeeRequest request);

    EmployeeResponse assignRole(String employeeId, AssignRoleRequest request);

    EmployeeResponse revokeRole(String employeeId);

    EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request);


    EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request);
}
