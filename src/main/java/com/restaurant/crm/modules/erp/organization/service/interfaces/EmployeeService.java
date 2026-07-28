package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.EmployeeBranchAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.EmployeeBranchAssignmentResponse;

public interface EmployeeService {

    EmployeeBranchAssignmentResponse getBranchManager(String branchId);

    EmployeeBranchAssignmentResponse assignToBranch(
            String branchId,
            EmployeeBranchAssignmentRequest request
    );

    EmployeeBranchAssignmentResponse removeManager(String branchId);
}
