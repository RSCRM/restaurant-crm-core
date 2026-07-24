package com.restaurant.crm.modules.erp.manage_branch_manager.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.request.BranchManagerCreationRequest;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.request.BranchManagerUpdateRequest;
import com.restaurant.crm.modules.erp.manage_branch_manager.dto.response.BranchManagerResponse;

public interface BranchManagerService {
    BranchManagerResponse create(BranchManagerCreationRequest request);

    PagingResponse<BranchManagerResponse> getBranchManagers(PagingRequest request, String branchId);

    BranchManagerResponse getById(String branchManagerId);

    BranchManagerResponse update(String branchManagerId, BranchManagerUpdateRequest request);

    void deleteById(String branchManagerId);
}
