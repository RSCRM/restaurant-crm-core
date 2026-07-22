package com.restaurant.crm.modules.erp.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.dto.response.LicenseInfoResponse;

public interface ContractService {
    PagingResponse<LicenseInfoResponse> getMyLicenseInfos(PagingRequest request);

    LicenseInfoResponse getLicenseInfoByOrganization(String organizationId);
}
