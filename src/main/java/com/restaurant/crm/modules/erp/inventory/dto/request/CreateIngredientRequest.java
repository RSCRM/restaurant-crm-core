package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.constants.IngredientConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateIngredientRequest {

    @NotBlank
    String branchId;

    @NotBlank
    String ingredientCategoryId;

    @NotBlank
    @Size(max = IngredientConstants.MAX_CHARS_INGREDIENT_NAME)
    String ingredientName;

    @NotBlank
    @Size(max = IngredientConstants.MAX_CHARS_UNIT)
    String unit;

    @Size(max = IngredientConstants.MAX_CHARS_DESCRIPTION)
    String description;
}
