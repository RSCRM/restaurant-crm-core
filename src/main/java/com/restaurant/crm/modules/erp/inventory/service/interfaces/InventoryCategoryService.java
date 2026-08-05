package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryCategoryResponse;

public interface InventoryCategoryService {

    InventoryCategoryResponse createInventoryCategory(
        CreateInventoryCategoryRequest request
    );

    InventoryCategoryResponse getInventoryCategoryById(
        String id
    );

    PagingResponse<InventoryCategoryResponse> getInventoryCategories(
        int page,
        int size
    );

    PagingResponse<InventoryCategoryResponse> searchInventoryCategories(
        String categoryName,
        int page,
        int size
    );

    InventoryCategoryResponse updateInventoryCategory(
        String id,
        UpdateInventoryCategoryRequest request
    );

    void deleteInventoryCategory(
        String id
    );
}