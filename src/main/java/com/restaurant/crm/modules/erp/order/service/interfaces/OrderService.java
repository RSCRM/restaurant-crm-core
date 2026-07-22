package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.ModifyOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;

public interface OrderService {

    AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request);

    void modifyOrderItem(String orderId, String orderItemId, ModifyOrderItemRequestDto request);
}
