package com.restaurant.crm.modules.erp.inventory.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
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
    String ingredientId;

    @DecimalMin(
        value = "0.0",
        inclusive = true
    )
    BigDecimal quantity;

    @DecimalMin(
        value = "0.0",
        inclusive = true
    )
    BigDecimal minimumQuantity;
}
