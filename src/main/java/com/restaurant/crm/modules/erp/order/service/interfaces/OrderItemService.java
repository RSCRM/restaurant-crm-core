package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemStatusRequest;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;


public interface OrderItemService {

    AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request);

    void updateOrderItemQuantity(String orderId, String orderItemId, UpdateOrderItemQuantityRequestDto request);

    void updateOrderItemModifiers(String orderId, String orderItemId, UpdateOrderItemModifiersRequestDto request);

    void removeOrderItem(String orderId, String orderItemId);

    OrderItemResponse updateStatus(String orderItemId, UpdateOrderItemStatusRequest request);
}
