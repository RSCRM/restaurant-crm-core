package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;

import java.time.Instant;

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

    PagingResponse<InventoryTransactionResponse> getTransactionsByType(
        String branchId,
        InventoryTransactionType type,
        int page,
        int size
    );

    PagingResponse<InventoryTransactionResponse> getTransactionsByDateRange(
        String branchId,
        Instant from,
        Instant to,
        int page,
        int size
    );
}