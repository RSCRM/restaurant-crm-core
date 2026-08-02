package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;

public interface InventoryService {
    InventoryResponse createInventory(
        CreateInventoryRequest request
    );

    InventoryResponse getInventoryById(
        String id
    );

    InventoryResponse getInventoryByIngredientId(
        String ingredientId
    );

    PagingResponse<InventoryResponse> getInventoriesByBranch(
        int page,
        int size
    );

    InventoryResponse updateInventory(
        String id,
        UpdateInventoryRequest request
    );

    PagingResponse<InventoryResponse> getInventoriesByStatus(
        InventoryStatus status,
        int page,
        int size
    );
}
