package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientResponse;

public interface IngredientService {

    IngredientResponse createIngredient(
            CreateIngredientRequest request
    );

    IngredientResponse getIngredientById(
            String id
    );

    PagingResponse<IngredientResponse> getIngredientsByBranch(
            int page,
            int size
    );

    PagingResponse<IngredientResponse> searchIngredients(
            String ingredientName,
            int page,
            int size
    );

    IngredientResponse updateIngredient(
            String id,
            UpdateIngredientRequest request
    );

    void deleteIngredient(
            String id
    );

    PagingResponse<IngredientResponse> getIngredientsByCategory(
        String categoryId,
        int page,
        int size
    );
}
