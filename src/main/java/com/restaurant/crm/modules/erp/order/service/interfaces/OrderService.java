package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;

public interface OrderService {

    CreateOrderResponse create(CreateOrderRequestDto request);

    CancelOrderResponse cancelOrder(String orderId);

    OrderCookingStatusResponse getOrderCookingStatus(String orderId);

    OrderCookingStatusResponse getActiveOrderCookingStatusByTable(String tableId);
}
