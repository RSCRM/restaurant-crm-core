package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemModifiersRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemQuantityRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;

/**
 * Service interface for managing order items.
 */
public interface OrderItemService {

    AddOrderItemResponse addOrderItem(String orderId, AddOrderItemRequestDto request);

    void updateOrderItemQuantity(String orderId, String orderItemId, UpdateOrderItemQuantityRequestDto request);

    void updateOrderItemModifiers(String orderId, String orderItemId, UpdateOrderItemModifiersRequestDto request);

    void removeOrderItem(String orderId, String orderItemId);

    /** Kitchen accepts the item for preparation: PENDING -> IN_PROGRESS (uc-scf-03). */
    OrderItemResponse accept(String orderItemId);

    /** Kitchen marks preparation done: IN_PROGRESS -> READY_TO_SERVE (uc-scf-05). */
    OrderItemResponse complete(String orderItemId);

    /** Kitchen releases an accepted item back to the queue: IN_PROGRESS -> PENDING. */
    OrderItemResponse release(String orderItemId);

    /** Kitchen cancels the item (reason required): PENDING/IN_PROGRESS -> CANCELLED (uc-scf-06). */
    OrderItemResponse cancel(String orderItemId, String reason);

    /** Service marks a no-preparation item ready: PENDING -> READY_TO_SERVE. */
    OrderItemResponse markReady(String orderItemId);

    /** Service delivers the item to the table: READY_TO_SERVE -> SERVED (uc-sw-14). */
    OrderItemResponse serve(String orderItemId);
}
