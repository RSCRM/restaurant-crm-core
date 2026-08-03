package com.restaurant.crm.modules.licensemanagement.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.LicenseSearchRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.UpdateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.DeleteLicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseDetailResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;

import java.util.List;

public interface LicenseService {

    LicenseResponse createLicense(CreateLicenseRequest request);

    PagingResponse<LicenseResponse> getLicenses(PagingRequest request);

    PagingResponse<LicenseResponse> searchLicenses(LicenseSearchRequest searchRequest, PagingRequest pagingRequest);

    LicenseResponse updateLicense(String id, UpdateLicenseRequest request);

    DeleteLicenseResponse deleteLicense(String id);

    LicenseResponse lockLicense(String id);

    LicenseResponse reactivateLicense(String id);

    LicenseDetailResponse getLicenseDetail(String id, int page, int size);

    List<LicenseResponse> getLicensesByOrganizationId(String organizationId);
}
