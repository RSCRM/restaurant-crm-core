package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.AssignRoleRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.SalaryConfigRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.ProfileUpdateAccessRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;

public interface EmployeeService {
    PagingResponse<EmployeeResponse> getEmployees(
            String organizationId,
            String branchId,
            String keyword,
            String role,
            String status,
            int page,
            int size,
            String field,
            String direction
    );

    EmployeeResponse getEmployee(String employeeId);

    EmployeeBranchAssignmentResponse assignToBranch(String branchId, EmployeeBranchAssignmentRequest request);

    EmployeeBranchAssignmentResponse getBranchManager(String branchId);

    EmployeeBranchAssignmentResponse removeBranchManager(String branchId);

    EmployeeResponse addEmployee(CreateEmployeeRequest request);

    EmployeeResponse updateEmployee(String employeeId, CreateEmployeeRequest request);

    EmployeeResponse deleteEmployee(String employeeId);

    EmployeeResponse enableEmployee(String employeeId);

    EmployeeResponse disableEmployee(String employeeId);

    EmployeeResponse assignRole(String employeeId, AssignRoleRequest request);

    EmployeeResponse revokeRole(String employeeId);

    EmployeeResponse configSalary(String employeeId, SalaryConfigRequest request);

    EmployeeResponse setProfileUpdateAccess(String employeeId, ProfileUpdateAccessRequest request);
}
