package com.restaurant.crm.modules.licensemanagement.mapper;

import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LicenseSubscriptionMapper {

    SubscriptionResponse toSubscriptionResponse(LicenseSubscription subscription);
}
