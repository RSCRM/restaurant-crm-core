package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventoryTransactionSearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryTransactionResponse;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryTransactionService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;

@RestController
@RequestMapping("/api/v1/erp/inventory-transactions")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryTransactionController {
    InventoryTransactionService inventoryTransactionService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_MANAGE')")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> createTransaction(
        @Valid @RequestBody CreateInventoryTransactionRequest request
    ) {
        InventoryTransactionResponse response =
            inventoryTransactionService.createTransaction(request);

        return ResponseEntity.ok(
            ApiResponse.<InventoryTransactionResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    public ResponseEntity<ApiResponse<InventoryTransactionResponse>> getTransactionById(
        @PathVariable String id
    ) {
        InventoryTransactionResponse response =
            inventoryTransactionService.getTransactionById(id);

        return ResponseEntity.ok(
            ApiResponse.<InventoryTransactionResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> getTransactions(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagingResponse<InventoryTransactionResponse> response =
            inventoryTransactionService.getTransactionsByBranch(
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(response)
                .build()
        );
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> searchTransactions(
        @RequestBody InventoryTransactionSearchRequest searchRequest,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size,
        @RequestParam(defaultValue = PaginationConstant.DESC) String direction,
        @RequestParam(defaultValue = "createdAt") String field
    ) {

        PagingRequest pagingRequest = PagingRequest.builder()
            .page(page)
            .pageSize(size)
            .sortRequest(
                SortRequest.builder()
                    .direction(direction)
                    .field(field)
                    .build()
            )
            .build();

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(
                    inventoryTransactionService.searchTransactions(
                        searchRequest,
                        pagingRequest
                    )
                )
                .build()
        );
    }
}
