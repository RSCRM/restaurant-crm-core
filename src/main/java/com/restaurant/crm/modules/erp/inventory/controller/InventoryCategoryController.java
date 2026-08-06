package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.InventoryCategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/inventory-categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class InventoryCategoryController {

    InventoryCategoryService inventoryCategoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('INVENTORY_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<InventoryCategoryResponse>> createInventoryCategory(
        @Valid @RequestBody CreateInventoryCategoryRequest request
    ) {

        InventoryCategoryResponse response =
            inventoryCategoryService.createInventoryCategory(request);

        return ResponseEntity.ok(
            ApiResponse.<InventoryCategoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryCategoryResponse>>> getInventoryCategories(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<InventoryCategoryResponse> response =
            inventoryCategoryService.getInventoryCategories(
                page,
                size
            );

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryCategoryResponse>>builder()
                .data(response)
                .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<InventoryCategoryResponse>> getInventoryCategoryById(
        @PathVariable String id
    ) {

        InventoryCategoryResponse response =
            inventoryCategoryService.getInventoryCategoryById(id);

        return ResponseEntity.ok(
            ApiResponse.<InventoryCategoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<InventoryCategoryResponse>> updateInventoryCategory(
        @PathVariable String id,
        @Valid @RequestBody UpdateInventoryCategoryRequest request
    ) {

        InventoryCategoryResponse response =
            inventoryCategoryService.updateInventoryCategory(
                id,
                request
            );

        return ResponseEntity.ok(
            ApiResponse.<InventoryCategoryResponse>builder()
                .data(response)
                .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INVENTORY_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteInventoryCategory(
        @PathVariable String id
    ) {

        inventoryCategoryService.deleteInventoryCategory(id);

        return ResponseEntity.ok(
            ApiResponse.<Void>builder()
                .build()
        );
    }
}