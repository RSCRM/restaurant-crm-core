package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
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
public class InventoryTransactionSearchRequest {

    // Search
    String ingredientName;

    // Filter
    InventoryTransactionType transactionType;

    InventoryTransactionDirection transactionDirection;

    BigDecimal quantityFrom;
    BigDecimal quantityTo;

    Instant transactionTimeFrom;
    Instant transactionTimeTo;

    String employeeId;
}