package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/inventories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryController {
    InventoryService inventoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<ApiResponse<InventoryResponse>> createInventory(
        @Valid @RequestBody CreateInventoryRequest request
    ) {
        InventoryResponse response =
            inventoryService.createInventory(request);

        return ResponseEntity.ok(
            ApiResponse.<InventoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryById(
        @PathVariable String id
    ) {
        InventoryResponse response =
            inventoryService.getInventoryById(id);

        return ResponseEntity.ok(
            ApiResponse.<InventoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/ingredient/{ingredientId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<InventoryResponse>> getInventoryByIngredientId(
        @PathVariable String ingredientId
    ) {
        InventoryResponse response =
            inventoryService.getInventoryByIngredientId(ingredientId);

        return ResponseEntity.ok(
            ApiResponse.<InventoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryResponse>>> getInventoriesByBranch(
        @PathVariable String branchId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {
        PagingResponse<InventoryResponse> response =
            inventoryService.getInventoriesByBranch(
                branchId,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryResponse>>builder()
                .data(response)
                .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_MANAGE')")
    public ResponseEntity<ApiResponse<InventoryResponse>> updateInventory(
        @PathVariable String id,
        @Valid @RequestBody UpdateInventoryRequest request
    ) {
        InventoryResponse response =
            inventoryService.updateInventory(
                id,
                request
            );

        return ResponseEntity.ok(
            ApiResponse.<InventoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    @GetMapping("/branch/{branchId}/status/{status}")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryResponse>>> getInventoriesByStatus(
        @PathVariable String branchId,
        @PathVariable InventoryStatus status,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {

        PagingResponse<InventoryResponse> response =
            inventoryService.getInventoriesByStatus(
                branchId,
                status,
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryResponse>>builder()
                .data(response)
                .build()
        );
    }
}