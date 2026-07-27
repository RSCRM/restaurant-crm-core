package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.response.KdsActiveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.KdsItemResponse;

import java.util.List;

public interface KdsService {
    KdsActiveResponse getActiveItems();
    List<KdsItemResponse> getHistoryItems();
}
