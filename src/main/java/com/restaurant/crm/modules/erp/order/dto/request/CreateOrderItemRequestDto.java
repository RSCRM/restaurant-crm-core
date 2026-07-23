package com.restaurant.crm.modules.erp.order.dto.request;

import com.restaurant.crm.modules.erp.order.constants.OrderConstants;
import jakarta.validation.Valid;
import jakarta.validation.constraints.AssertTrue;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateOrderItemRequestDto {

    String productId;

    String comboId;

    @NotNull
    @Min(OrderConstants.MIN_QUANTITY)
    Integer quantity;

    @Size(max = OrderConstants.MAX_CHARS_NOTE)
    String note;

    @Valid
    List<CreateOrderItemModifierRequestDto> modifiers;

    @AssertTrue(message = "Either productId or comboId must be provided, but not both")
    public boolean isProductOrComboValid() {
        boolean hasProduct = productId != null && !productId.isBlank();
        boolean hasCombo = comboId != null && !comboId.isBlank();
        return hasProduct ^ hasCombo;
    }
}
