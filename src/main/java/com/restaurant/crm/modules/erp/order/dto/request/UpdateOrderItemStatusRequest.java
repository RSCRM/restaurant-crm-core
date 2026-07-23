package com.restaurant.crm.modules.erp.order.dto.request;

import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

/**
 * Request DTO representing the new status for an order item.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateOrderItemStatusRequest {

    @NotNull(message = "Status cannot be null")
    OrderItemStatus status;
}
