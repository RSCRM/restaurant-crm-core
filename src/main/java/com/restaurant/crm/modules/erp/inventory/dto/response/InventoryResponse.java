package com.restaurant.crm.modules.erp.inventory.dto.response;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventoryResponse {
    String id;
    String ingredientId;
    String ingredientName;
    BigDecimal quantity;
    BigDecimal minimumQuantity;
    InventoryStatus status;
    Instant createdAt;
    Instant updatedAt;
}
