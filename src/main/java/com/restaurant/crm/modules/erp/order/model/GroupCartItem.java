package com.restaurant.crm.modules.erp.order.model;

import java.time.Instant;
import java.util.List;


public record GroupCartItem(
        String cartItemId,
        String productId,
        String comboId,
        Integer quantity,
        String note,
        List<String> modifierOptionIds,
        String addedByDeviceId,
        Instant addedAt
) {
}
