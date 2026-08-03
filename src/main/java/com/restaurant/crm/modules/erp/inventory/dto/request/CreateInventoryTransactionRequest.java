package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateInventoryTransactionRequest {

    @NotBlank
    String inventoryId;

    String employeeId;

    @NotNull
    InventoryTransactionType transactionType;

    @NotNull
    InventoryTransactionDirection transactionDirection;

    @NotNull
    @DecimalMin(
        value = "0.001"
    )
    BigDecimal quantity;

    String note;
}
