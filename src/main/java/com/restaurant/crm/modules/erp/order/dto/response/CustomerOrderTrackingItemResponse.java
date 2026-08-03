package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.CustomerOrderStage;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerOrderTrackingItemResponse {

    String orderItemId;
    String itemName;
    Integer quantity;
    String note;
    OrderItemStatus status;
    CustomerOrderStage customerStage;
    Integer stageOrder;
    Instant updatedAt;
}
