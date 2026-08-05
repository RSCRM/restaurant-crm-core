package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.InventorySearchRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateInventoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.InventoryResponse;
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

    @GetMapping
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryResponse>>> getInventoriesByBranch(
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<InventoryResponse> response =
            inventoryService.getInventoriesByBranch(page, size);

        return ResponseEntity.ok(
            ApiResponse.<PagingResponse<InventoryResponse>>builder()
                .data(response)
                .build()
        );
    }

    @PostMapping("/search")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryResponse>>> searchInventories(
        @RequestBody InventorySearchRequest searchRequest,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size,
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
            ApiResponse.<PagingResponse<InventoryResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(
                    inventoryService.searchInventories(
                        searchRequest,
                        pagingRequest
                    )
                )
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

    @GetMapping("/category/{categoryId}")
    @PreAuthorize("hasAuthority('INVENTORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<InventoryResponse>>> getInventoriesByCategory(
        @PathVariable String categoryId,
        @RequestParam(value = "page", defaultValue = "1") int page,
        @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<InventoryResponse> response =
            inventoryService.getInventoriesByCategory(
                categoryId,
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
            inventoryService.updateInventory(id, request);

        return ResponseEntity.ok(
            ApiResponse.<InventoryResponse>builder()
                .data(response)
                .build()
        );
    }
}