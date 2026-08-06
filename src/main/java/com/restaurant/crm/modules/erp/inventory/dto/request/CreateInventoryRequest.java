package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.constants.InventoryConstants;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateInventoryRequest {

    @NotBlank
    String inventoryCategoryId;

    @NotBlank
    @Size(max = InventoryConstants.MAX_CHARS_INVENTORY_NAME)
    String inventoryName;

    @NotBlank
    @Size(max = InventoryConstants.MAX_CHARS_UNIT)
    String unit;

    @Size(max = InventoryConstants.MAX_CHARS_DESCRIPTION)
    String description;

    @DecimalMin(value = "0.0", inclusive = true)
    BigDecimal minimumQuantity;
}