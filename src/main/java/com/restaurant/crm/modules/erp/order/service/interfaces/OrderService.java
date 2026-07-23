package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;

public interface OrderService {

    CreateOrderResponse create(CreateOrderRequestDto request);
    com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse getOrderCookingStatus(String orderId);
    com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse getActiveOrderCookingStatusByTable(String tableId);
}
