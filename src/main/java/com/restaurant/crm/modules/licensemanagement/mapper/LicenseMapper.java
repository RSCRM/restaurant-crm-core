package com.restaurant.crm.modules.licensemanagement.mapper;

import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;
import com.restaurant.crm.modules.licensemanagement.entity.License;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LicenseMapper {

    License toLicense(CreateLicenseRequest request);

    LicenseResponse toLicenseResponse(License license);
}
