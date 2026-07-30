package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateEmployeeRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeResponse;

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

    EmployeeResponse createEmployee(CreateEmployeeRequest request);

    EmployeeResponse updateEmployee(String employeeId, UpdateEmployeeRequest request);

    EmployeeResponse softDeleteEmployee(String employeeId);

    EmployeeResponse enableEmployee(String employeeId);

    EmployeeResponse disableEmployee(String employeeId);

    EmployeeBranchAssignmentResponse getBranchManager(String branchId);

    EmployeeBranchAssignmentResponse assignToBranch(
            String branchId,
            EmployeeBranchAssignmentRequest request
    );

    EmployeeBranchAssignmentResponse removeManager(String branchId);
}
