package com.restaurant.crm.modules.licensemanagement.mapper;

import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseInfo;
import com.restaurant.crm.modules.licensemanagement.dto.response.SubscriptionResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import com.restaurant.crm.modules.licensemanagement.entity.LicenseSubscription;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LicenseSubscriptionMapper {

    @Mapping(target = "id", source = "subscription.id")
    @Mapping(target = "organizationId", source = "subscription.organizationId")
    @Mapping(target = "startDate", source = "subscription.startDate")
    @Mapping(target = "endDate", source = "subscription.endDate")
    @Mapping(target = "status", source = "subscription.status")
    @Mapping(target = "price", source = "subscription.price")
    @Mapping(target = "billingCycle", source = "subscription.billingCycle")
    @Mapping(target = "maxBranch", source = "subscription.maxBranch")
    @Mapping(target = "maxEmployee", source = "subscription.maxEmployee")
    @Mapping(target = "createdAt", source = "subscription.createdAt")
    @Mapping(target = "updatedAt", source = "subscription.updatedAt")
    @Mapping(target = "license", source = "license")
    SubscriptionResponse toSubscriptionResponse(LicenseSubscription subscription, License license);

    LicenseInfo toLicenseInfo(License license);
}
