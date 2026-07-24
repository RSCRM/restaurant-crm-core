package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * One card on the kitchen display board (uc-scf-01).
 * {@code createdAt} is returned so the client can show how long the item has waited.
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KitchenOrderItemResponse {
    String orderItemId;
    String itemName;
    Integer quantity;
    String tableNumber;
    String note;
    OrderItemStatus status;
    boolean priorityFlag;
    Instant createdAt;
}
