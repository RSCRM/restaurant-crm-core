package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventorySearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;

public interface InventoryService {

    InventoryResponse createInventory(
        CreateInventoryRequest request
    );

    InventoryResponse getInventoryById(
        String id
    );

    PagingResponse<InventoryResponse> getInventoriesByBranch(
        int page,
        int size
    );

    PagingResponse<InventoryResponse> getInventoriesByCategory(
        String categoryId,
        int page,
        int size
    );

    PagingResponse<InventoryResponse> searchInventories(
        InventorySearchRequest searchRequest,
        PagingRequest pagingRequest
    );

    InventoryResponse updateInventory(
        String id,
        UpdateInventoryRequest request
    );

    void deleteInventory(
        String id
    );
}