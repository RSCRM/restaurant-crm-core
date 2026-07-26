package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryTransactionRequest;
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

    @GetMapping("/inventory/{inventoryId}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> getTransactionsByInventory(
        @PathVariable String inventoryId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagingResponse<InventoryTransactionResponse> response =
            inventoryTransactionService.getTransactionsByInventory(
                inventoryId,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> getTransactionsByBranch(
        @PathVariable String branchId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagingResponse<InventoryTransactionResponse> response =
            inventoryTransactionService.getTransactionsByBranch(
                branchId,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(response)
                .build()
        );
    }

    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    @GetMapping("/branch/{branchId}/type/{type}")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> getTransactionsByType(
        @PathVariable String branchId,
        @PathVariable InventoryTransactionType type,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {

        PagingResponse<InventoryTransactionResponse> response =
            inventoryTransactionService.getTransactionsByType(
                branchId,
                type,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(response)
                .build()
        );
    }

    @PreAuthorize("hasAuthority('INVENTORY_TRANSACTION_VIEW')")
    @GetMapping("/branch/{branchId}/date-range")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryTransactionResponse>>> getTransactionsByDateRange(
        @PathVariable String branchId,
        @RequestParam Instant from,
        @RequestParam Instant to,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {

        PagingResponse<InventoryTransactionResponse> response =
            inventoryTransactionService.getTransactionsByDateRange(
                branchId,
                from,
                to,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryTransactionResponse>>builder()
                .data(response)
                .build()
        );
    }
}
