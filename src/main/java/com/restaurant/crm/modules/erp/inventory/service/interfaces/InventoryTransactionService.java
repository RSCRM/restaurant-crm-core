package com.restaurant.crm.modules.erp.inventory.service.interfaces;

import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateBatchInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventoryTransactionSearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;

import java.time.Instant;
import java.util.List;

public interface InventoryTransactionService {
    InventoryTransactionResponse createTransaction(
        CreateInventoryTransactionRequest request
    );

    InventoryTransactionResponse getTransactionById(
        String id
    );

    PagingResponse<InventoryTransactionResponse> getTransactionsByBranch(
        int page,
        int size
    );

    PagingResponse<InventoryTransactionResponse> searchTransactions(
        InventoryTransactionSearchRequest searchRequest,
        PagingRequest pagingRequest
    );

    List<InventoryTransactionResponse> createBatchTransactions(
        CreateBatchInventoryTransactionRequest request
    );
}