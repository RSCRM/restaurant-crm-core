package com.restaurant.crm.modules.erp.inventory.dto.response;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Getter
@Setter
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class IngredientResponse {
    String id;
    String branchId;
    String ingredientCategoryId;
    String ingredientName;
    String unit;
    String description;
    Instant createdAt;
    Instant updatedAt;
}
