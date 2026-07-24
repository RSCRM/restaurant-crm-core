package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientCategoryResponse;

public interface IngredientCategoryService {

    IngredientCategoryResponse createIngredientCategory(
            CreateIngredientCategoryRequest request
    );

    IngredientCategoryResponse getIngredientCategoryById(
            String id
    );

    PagingResponse<IngredientCategoryResponse> getIngredientCategories(
            String branchId,
            int page,
            int size
    );

    IngredientCategoryResponse updateIngredientCategory(
            String id,
            UpdateIngredientCategoryRequest request
    );

    void deleteIngredientCategory(
            String id
    );
}