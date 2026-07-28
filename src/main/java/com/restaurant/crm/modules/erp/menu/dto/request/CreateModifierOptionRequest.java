package com.restaurant.crm.modules.erp.menu.dto.request;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateModifierOptionRequest {
    @NotBlank(message = "MODIFIER_OPTION_NAME_REQUIRED")
    @Size(max = 100)
    String optionName;
    @NotNull
    @DecimalMin("0")
    BigDecimal additionalPrice;
    @Size(max = 20)
    String status;
}
