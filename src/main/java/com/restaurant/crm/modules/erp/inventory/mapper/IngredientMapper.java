package com.restaurant.crm.modules.erp.inventory.mapper;

import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientResponse;
import com.restaurant.crm.modules.erp.inventory.entity.Ingredient;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IngredientMapper {

    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "ingredientCategory", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    Ingredient toIngredient(
            CreateIngredientRequest request
    );

    @Mapping(
            source = "branch.id",
            target = "branchId"
    )
    @Mapping(
            source = "ingredientCategory.id",
            target = "ingredientCategoryId"
    )
    IngredientResponse toIngredientResponse(
            Ingredient ingredient
    );

    @Mapping(target = "id", ignore = true)

    // Don't allow changing relationships through mapper
    @Mapping(target = "branch", ignore = true)
    @Mapping(target = "ingredientCategory", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateIngredient(
            UpdateIngredientRequest request,
            @MappingTarget Ingredient ingredient
    );
}