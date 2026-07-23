package com.restaurant.crm.modules.erp.order.dto.request;

import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
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
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderItemModifierRequestDto {

    @NotNull
    @NotBlank
    String modifierOptionId;

    @Builder.Default
    @NotNull
    @Min(OrderConstants.MIN_QUANTITY)
    Integer quantity = OrderConstants.MIN_QUANTITY;

    @Builder.Default
    @NotNull
    BigDecimal additionalPrice = BigDecimal.ZERO;
}
