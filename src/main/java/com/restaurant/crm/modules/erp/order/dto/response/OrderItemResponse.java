package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;


@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class OrderItemResponse {
    String orderItemId;
    String orderId;
    String productId;
    String comboId;
    Integer quantity;
    OrderItemStatus status;
}
