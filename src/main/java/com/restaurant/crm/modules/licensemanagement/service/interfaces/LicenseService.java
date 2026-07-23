package com.restaurant.crm.modules.licensemanagement.service.interfaces;

import com.restaurant.crm.modules.licensemanagement.dto.request.CreateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.request.UpdateLicenseRequest;
import com.restaurant.crm.modules.licensemanagement.dto.response.DeleteLicenseResponse;
import com.restaurant.crm.modules.licensemanagement.dto.response.LicenseResponse;

public interface LicenseService {

    LicenseResponse createLicense(CreateLicenseRequest request);

    LicenseResponse updateLicense(String id, UpdateLicenseRequest request);

    DeleteLicenseResponse deleteLicense(String id);
}
