package com.restaurant.crm.modules.crm.pointwallet.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;

public interface PointWalletService {
    CustomerPointResponse getWallet(String customerId, String organizationId);
    CustomerPointResponse initializeWallet(String customerId, String organizationId);
    PagingResponse<CustomerPointHistoryResponse> getHistory(String customerId, String organizationId, int page, int size);
    CustomerPointResponse earnPoints(String customerId, String organizationId, int points, String orderId);
    CustomerPointResponse deductPoints(String customerId, String organizationId, int points, String referenceId);
    PagingResponse<CustomerPointResponse> getOrganizationCustomers(
            String organizationId,
            String searchPhone,
            Integer minPoints,
            Integer maxPoints,
            Integer minLifetimePoints,
            Integer maxLifetimePoints,
            int page,
            int size,
            String sortBy,
            String sortDirection
    );
}
