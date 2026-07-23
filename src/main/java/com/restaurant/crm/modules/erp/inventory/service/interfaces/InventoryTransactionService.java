package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;

public interface InventoryTransactionService {
    InventoryTransactionResponse createTransaction(
        CreateInventoryTransactionRequest request
    );

    InventoryTransactionResponse getTransactionById(
        String id
    );

    PagingResponse<InventoryTransactionResponse> getTransactionsByInventory(
        String inventoryId,
        int page,
        int size
    );

    PagingResponse<InventoryTransactionResponse> getTransactionsByBranch(
        String branchId,
        int page,
        int size
    );
}