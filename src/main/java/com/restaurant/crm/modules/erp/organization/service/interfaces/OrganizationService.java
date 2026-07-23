package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;

public interface OrganizationService {

    OrganizationResponse createOrganization(CreateOrganizationRequest request);

    OrganizationResponse getOrganizationById(String id);

    OrganizationResponse getOrganizationByOwnerId(String ownerId);

    PagingResponse<OrganizationResponse> getOrganizations(int page, int size);

    OrganizationResponse updateOrganization(
            String id,
            UpdateOrganizationRequest request
    );

    void deleteOrganization(String id);
}
