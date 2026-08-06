package com.restaurant.crm.modules.erp.menu.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateCategoryRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.CategoryResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CategoryManagementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/categories")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CategoryController {

    CategoryManagementService service;

    @PostMapping
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.CATEGORY_ADD + "')")
    public ResponseEntity<ApiResponse<CategoryResponse>> create(@Valid @RequestBody CreateCategoryRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<CategoryResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.create(request)).build());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.CATEGORY_UPDATE + "')")
    public ResponseEntity<ApiResponse<CategoryResponse>> update(@PathVariable String id, @Valid @RequestBody UpdateCategoryRequest request) {
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.update(id, request)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.CATEGORY_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CategoryResponse>>> list(@RequestParam String branchId) {
        return ResponseEntity.ok(ApiResponse.<List<CategoryResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listByBranch(branchId)).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CategoryResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<CategoryResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.get(id)).build());
    }
}
