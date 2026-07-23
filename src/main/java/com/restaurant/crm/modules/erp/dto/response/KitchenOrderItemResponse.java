package com.restaurant.crm.modules.erp.dto.response;

import com.restaurant.crm.modules.erp.enums.OrderItemStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * One kitchen-display card for the FIFO waiting list (uc-scf-01).
 * Fields follow the acceptance criteria in docs/kds/order-item-state-machine.md (section 5).
 */
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class KitchenOrderItemResponse {
    String orderItemId;
    String dishName;
    Integer quantity;
    String tableNumber;
    String note;
    OrderItemStatus status;
    Boolean priorityFlag;
    Instant createdAt;
}
