package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;

public interface OrderService {

    AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request);

    void updateOrderItemQuantity(
            String orderId,
            String orderItemId,
            UpdateOrderItemQuantityRequestDto request
    );

    void updateOrderItemModifiers(
            String orderId,
            String orderItemId,
            UpdateOrderItemModifiersRequestDto request
    );
}
