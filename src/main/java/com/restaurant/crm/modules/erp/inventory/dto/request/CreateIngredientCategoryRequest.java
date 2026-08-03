package com.restaurant.crm.modules.erp.inventory.dto.request;

import com.restaurant.crm.modules.erp.inventory.constants.IngredientCategoryConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateIngredientCategoryRequest {

    @NotBlank
    @Size(max = IngredientCategoryConstants.MAX_CHARS_CATEGORY_NAME)
    String categoryName;

    @Size(max = IngredientCategoryConstants.MAX_CHARS_DESCRIPTION)
    String description;
}
