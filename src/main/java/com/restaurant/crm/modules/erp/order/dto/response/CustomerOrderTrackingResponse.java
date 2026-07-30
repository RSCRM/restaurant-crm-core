package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Customer view of the current order's cooking progress (uc-c-06).
 * Deliberately omits {@code customerPhone} (the owner's phone) so members cannot read it.
 * When the session has no order yet, {@code hasActiveOrder} is false and {@code items} is empty.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CustomerOrderTrackingResponse {

    boolean hasActiveOrder;
    String orderId;
    String orderCode;
    String tableId;
    OrderStatus orderStatus;
    BigDecimal subtotal;
    BigDecimal discountAmount;
    BigDecimal totalAmount;
    CustomerOrderTrackingSummaryResponse summary;

    @Builder.Default
    List<CustomerOrderTrackingItemResponse> items = new ArrayList<>();

    Instant updatedAt;
}
