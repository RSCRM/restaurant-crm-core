package com.restaurant.crm.modules.crm.point_wallet.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.point_wallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.point_wallet.dto.response.CustomerPointResponse;

public interface PointWalletService {
    CustomerPointResponse getWallet(String customerId, String restaurantId);
    CustomerPointResponse initializeWallet(String customerId, String restaurantId);
    PagingResponse<CustomerPointHistoryResponse> getHistory(String customerId, String restaurantId, int page, int size);
    CustomerPointResponse earnPoints(String customerId, String restaurantId, int points, String orderId);
    CustomerPointResponse deductPoints(String customerId, String restaurantId, int points, String referenceId);
}
