package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientCategoryRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientCategoryResponse;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.IngredientCategoryService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/ingredient-categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientCategoryController {

    IngredientCategoryService ingredientCategoryService;

    @PostMapping
    @PreAuthorize("hasAuthority('INGREDIENT_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<IngredientCategoryResponse>> createIngredientCategory(
            @Valid @RequestBody CreateIngredientCategoryRequest request
    ) {

        IngredientCategoryResponse response =
                ingredientCategoryService.createIngredientCategory(request);

        return ResponseEntity.ok(
                ApiResponse.<IngredientCategoryResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/branch/{branchId}")
    @PreAuthorize("hasAuthority('INGREDIENT_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<IngredientCategoryResponse>>> getIngredientCategories(
            @PathVariable String branchId,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<IngredientCategoryResponse> response =
                ingredientCategoryService.getIngredientCategories(
                        branchId,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<IngredientCategoryResponse>>builder()
                        .data(response)
                        .build()
        );
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_CATEGORY_VIEW')")
    public ResponseEntity<ApiResponse<IngredientCategoryResponse>> getIngredientCategoryById(
            @PathVariable String id
    ) {

        IngredientCategoryResponse response =
                ingredientCategoryService.getIngredientCategoryById(id);

        return ResponseEntity.ok(
                ApiResponse.<IngredientCategoryResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<IngredientCategoryResponse>> updateIngredientCategory(
            @PathVariable String id,
            @Valid @RequestBody UpdateIngredientCategoryRequest request
    ) {

        IngredientCategoryResponse response =
                ingredientCategoryService.updateIngredientCategory(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<IngredientCategoryResponse>builder()
                        .data(response)
                        .build()
        );
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_CATEGORY_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteIngredientCategory(
            @PathVariable String id
    ) {

        ingredientCategoryService.deleteIngredientCategory(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .build()
        );
    }
}