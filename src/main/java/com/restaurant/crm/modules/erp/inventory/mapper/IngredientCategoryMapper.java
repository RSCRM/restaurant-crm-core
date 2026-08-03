package com.restaurant.crm.modules.erp.inventory.mapper;


import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.entity.IngredientCategory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface IngredientCategoryMapper {

    @Mapping(target = "id", ignore = true)

    // Set manually in service
    @Mapping(target = "branch", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    IngredientCategory toIngredientCategory(
            CreateIngredientCategoryRequest request
    );

    @Mapping(
            source = "branch.id",
            target = "branchId"
    )
    IngredientCategoryResponse toIngredientCategoryResponse(
            IngredientCategory ingredientCategory
    );

    @Mapping(target = "id", ignore = true)

    // Don't allow changing branch
    @Mapping(target = "branch", ignore = true)

    @Mapping(target = "createdBy", ignore = true)
    @Mapping(target = "updatedBy", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "version", ignore = true)
    void updateIngredientCategory(
            UpdateIngredientCategoryRequest request,
            @MappingTarget IngredientCategory ingredientCategory
    );
}
