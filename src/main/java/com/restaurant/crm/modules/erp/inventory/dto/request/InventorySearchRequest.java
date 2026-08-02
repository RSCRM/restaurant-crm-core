package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import lombok.*;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;
import java.time.Instant;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class InventorySearchRequest {

    // Search
    String ingredientName;

    // Filter
    InventoryStatus status;

    BigDecimal quantityFrom;
    BigDecimal quantityTo;

    BigDecimal minimumQuantityFrom;
    BigDecimal minimumQuantityTo;

    Instant createdAtFrom;
    Instant createdAtTo;
}
