package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationBranchRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationBranchResponse;

public interface OrganizationBranchService {

    OrganizationBranchResponse createOrganizationBranch(
            CreateOrganizationBranchRequest request
    );

    OrganizationBranchResponse getOrganizationBranchById(String id);

    PagingResponse<OrganizationBranchResponse> getOrganizationBranches(
            int page,
            int size
    );

    PagingResponse<OrganizationBranchResponse> getOrganizationBranchesByOrganization(
            String organizationId,
            int page,
            int size
    );

    OrganizationBranchResponse updateOrganizationBranch(
            String id,
            UpdateOrganizationBranchRequest request
    );

    void deleteOrganizationBranch(String id);
}
