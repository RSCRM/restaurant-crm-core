package com.restaurant.crm.modules.licensemanagement.service.interfaces;

import com.restaurant.crm.modules.licensemanagement.dto.request.GrantSubscriptionRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;

public interface LicenseSubscriptionService {

    SubscriptionResponse grantSubscription(GrantSubscriptionRequest request);

    SubscriptionResponse renewSubscription(String id);

    SubscriptionResponse revokeSubscription(String id);
}
