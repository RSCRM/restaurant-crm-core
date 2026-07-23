package com.restaurant.crm.modules.erp.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateInventoryRequest {

    @DecimalMin(
        value = "0.0",
        inclusive = true
    )
    BigDecimal minimumQuantity;
}