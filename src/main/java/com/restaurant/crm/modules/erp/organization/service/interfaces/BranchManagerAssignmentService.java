package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.modules.erp.organization.dto.request.BranchManagerAssignmentRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.BranchManagerAssignmentResponse;

public interface BranchManagerAssignmentService {
    BranchManagerAssignmentResponse assignToBranch(String branchId, BranchManagerAssignmentRequest request);
}
