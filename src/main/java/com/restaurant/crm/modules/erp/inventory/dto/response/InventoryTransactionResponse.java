package com.restaurant.crm.modules.erp.inventory.dto.response;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
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
public class InventoryTransactionResponse {
    String id;
    String inventoryId;
    String employeeId;
    InventoryTransactionType transactionType;
    InventoryTransactionDirection transactionDirection;
    BigDecimal quantity;
    String note;
    String referenceId;
    Instant transactionTime;
    Instant createdAt;
    Instant updatedAt;
}