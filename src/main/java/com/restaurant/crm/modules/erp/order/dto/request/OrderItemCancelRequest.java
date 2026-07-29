package com.restaurant.crm.modules.erp.order.dto.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request body for cancelling an order item. The reason is mandatory and validated
 * in the service layer (ORDER_ITEM_CANCEL_REASON_REQUIRED).
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemCancelRequest {

    String reason;
}
