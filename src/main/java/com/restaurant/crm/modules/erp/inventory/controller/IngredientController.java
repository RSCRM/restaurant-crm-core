package com.restaurant.crm.modules.erp.inventory.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.inventory.dto.request.CreateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.request.UpdateIngredientRequest;
import com.restaurant.crm.modules.erp.inventory.dto.response.IngredientResponse;
import com.restaurant.crm.modules.erp.inventory.service.interfaces.IngredientService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/erp/ingredients")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class IngredientController {
    IngredientService ingredientService;

    @PostMapping
    @PreAuthorize("hasAuthority('INGREDIENT_MANAGE')")
    public ResponseEntity<ApiResponse<IngredientResponse>> createIngredient(
            @Valid @RequestBody CreateIngredientRequest request
    ) {

        IngredientResponse response =
                ingredientService.createIngredient(request);

        return ResponseEntity.ok(
                ApiResponse.<IngredientResponse>builder()
                        .data(response)
                        .build()
        );
    }


    @GetMapping
    @PreAuthorize("hasAuthority('INGREDIENT_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<IngredientResponse>>> getIngredients(
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<IngredientResponse> response =
                ingredientService.getIngredientsByBranch(
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<IngredientResponse>>builder()
                        .data(response)
                        .build()
        );
    }


    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_VIEW')")
    public ResponseEntity<ApiResponse<IngredientResponse>> getIngredientById(
            @PathVariable String id
    ) {

        IngredientResponse response =
                ingredientService.getIngredientById(id);

        return ResponseEntity.ok(
                ApiResponse.<IngredientResponse>builder()
                        .data(response)
                        .build()
        );
    }


    @GetMapping("/search")
    @PreAuthorize("hasAuthority('INGREDIENT_VIEW')")
    public ResponseEntity<ApiResponse<PagingResponse<IngredientResponse>>> searchIngredients(
            @RequestParam String ingredientName,
            @RequestParam(value = "page", defaultValue = "1") int page,
            @RequestParam(value = "size", defaultValue = "10") int size
    ) {

        PagingResponse<IngredientResponse> response =
                ingredientService.searchIngredients(
                        ingredientName,
                        page,
                        size
                );

        return ResponseEntity.ok(
                ApiResponse.<PagingResponse<IngredientResponse>>builder()
                        .data(response)
                        .build()
        );
    }


    @PatchMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_MANAGE')")
    public ResponseEntity<ApiResponse<IngredientResponse>> updateIngredient(
            @PathVariable String id,
            @Valid @RequestBody UpdateIngredientRequest request
    ) {

        IngredientResponse response =
                ingredientService.updateIngredient(
                        id,
                        request
                );

        return ResponseEntity.ok(
                ApiResponse.<IngredientResponse>builder()
                        .data(response)
                        .build()
        );
    }


    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('INGREDIENT_MANAGE')")
    public ResponseEntity<ApiResponse<Void>> deleteIngredient(
            @PathVariable String id
    ) {

        ingredientService.deleteIngredient(id);

        return ResponseEntity.ok(
                ApiResponse.<Void>builder()
                        .build()
        );
    }

    @PreAuthorize("hasAuthority('INGREDIENT_VIEW')")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<ApiResponse<PagingResponse<IngredientResponse>>> getIngredientsByCategory(
        @PathVariable String categoryId,
        @RequestParam(defaultValue = "1") int page,
        @RequestParam(defaultValue = "10") int size
    ) {
        PagingResponse<IngredientResponse> response =
            ingredientService.getIngredientsByCategory(categoryId, page, size);

        return ResponseEntity.ok(ApiResponse.<PagingResponse<IngredientResponse>>builder()
            .data(response)
            .build());
    }
}
