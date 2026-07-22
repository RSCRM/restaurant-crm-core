package com.restaurant.crm.modules.erp.mapper;

import com.restaurant.crm.modules.erp.dto.response.LicenseInfoResponse;
import com.restaurant.crm.modules.erp.entity.LicenseContract;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LicenseContractMapper {

    @Mapping(target = "licenseId", source = "id")
    @Mapping(target = "organizationId", source = "organization.id")
    @Mapping(target = "organizationName", source = "organization.organizationName")
    @Mapping(target = "licensePlanId", source = "licensePlan.id")
    @Mapping(target = "licensePlanName", source = "licensePlan.planName")
    @Mapping(target = "price", source = "licensePlan.price")
    @Mapping(target = "durationDays", source = "licensePlan.durationDays")
    @Mapping(target = "maxBranches", source = "licensePlan.maxBranches")
    LicenseInfoResponse toLicenseInfoResponse(LicenseContract licenseContract);
}
