package com.restaurant.crm.modules.erp.menu.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class CreateCategoryRequest {
    @NotBlank(message = "PRODUCT_BRANCH_REQUIRED")
    String branchId;
    @NotBlank(message = "CATEGORY_NAME_REQUIRED")
    @Size(max = 100)
    String categoryName;
    @Size(max = 255)
    String description;
    Integer displayOrder;
}
