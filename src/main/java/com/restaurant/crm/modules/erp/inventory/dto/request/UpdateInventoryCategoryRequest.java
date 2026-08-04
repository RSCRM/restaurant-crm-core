package com.restaurant.crm.modules.erp.inventory.dto.request;
import com.restaurant.crm.modules.erp.inventory.constants.InventoryCategoryConstants;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateInventoryCategoryRequest {

    @Size(max = InventoryCategoryConstants.MAX_CHARS_CATEGORY_NAME)
    String categoryName;

    @Size(max = InventoryCategoryConstants.MAX_CHARS_DESCRIPTION)
    String description;
}
