package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;

public interface EmployeeService {
    EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request);

    EmployeeResponse addEmployee(CreateEmployeeRequest request);

    EmployeeResponse assignRole(String employeeId, AssignRoleRequest request);

    EmployeeResponse revokeRole(String employeeId);
}
