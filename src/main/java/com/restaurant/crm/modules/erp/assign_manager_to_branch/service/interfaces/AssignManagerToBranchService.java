package com.restaurant.crm.modules.erp.assign_manager_to_branch.service.interfaces;

import com.restaurant.crm.modules.erp.assign_manager_to_branch.dto.request.BranchManagerAssignmentRequest;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.response.BranchManagerResponse;

public interface AssignManagerToBranchService {
    BranchManagerResponse assignToBranch(String branchId, BranchManagerAssignmentRequest request);
}
