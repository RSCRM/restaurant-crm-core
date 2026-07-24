package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;

/**
 * Service interface for managing order items.
 */
public interface OrderItemService {

    AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request);

    void updateOrderItemQuantity(String orderId, String orderItemId, UpdateOrderItemQuantityRequestDto request);

    void updateOrderItemModifiers(String orderId, String orderItemId, UpdateOrderItemModifiersRequestDto request);

    void removeOrderItem(String orderId, String orderItemId);

    /**
     * Updates the preparation status of an order item and triggers a notification if ready.
     *
     * @param orderItemId the ID of the order item to update
     * @param status the new status of the order item
     * @return the updated OrderItemResponse
     */
    OrderItemResponse updateStatus(String orderItemId, OrderItemStatus status);
}
