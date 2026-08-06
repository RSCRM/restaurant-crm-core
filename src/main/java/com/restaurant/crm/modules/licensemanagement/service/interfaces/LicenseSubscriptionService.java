package com.restaurant.crm.modules.licensemanagement.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.SubscriptionSearchRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;

public interface LicenseSubscriptionService {

    SubscriptionResponse grantSubscription(GrantSubscriptionRequest request);

    SubscriptionResponse renewSubscription(String id);

    SubscriptionResponse revokeSubscription(String id);

    PagingResponse<SubscriptionResponse> searchSubscriptionsByOrganization(
            String organizationId, SubscriptionSearchRequest searchRequest, PagingRequest pagingRequest);
}
