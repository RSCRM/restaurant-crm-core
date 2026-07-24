package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;

public interface OrderService {

    CreateOrderResponse create(CreateOrderRequestDto request);

    CancelOrderResponse cancelOrder(String orderId);
}
