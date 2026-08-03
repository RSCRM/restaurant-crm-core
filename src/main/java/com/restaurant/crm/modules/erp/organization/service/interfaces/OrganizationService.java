package com.restaurant.crm.modules.erp.organization.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.dto.request.CreateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.OrganizationSearchRequest;
import com.restaurant.crm.modules.erp.organization.dto.request.UpdateOrganizationRequest;
import com.restaurant.crm.modules.erp.organization.dto.response.OrganizationResponse;

public interface OrganizationService {

    OrganizationResponse createOrganization(CreateOrganizationRequest request);

    OrganizationResponse getOrganizationById(String id);

    OrganizationResponse getOrganizationByOwnerId(String ownerId);

    PagingResponse<OrganizationResponse> getOrganizations(int page, int size);

    PagingResponse<OrganizationResponse> searchOrganizations(
            OrganizationSearchRequest searchRequest, PagingRequest pagingRequest);

    OrganizationResponse updateOrganization(
            String id,
            UpdateOrganizationRequest request
    );

    void deleteOrganization(String id);
}
