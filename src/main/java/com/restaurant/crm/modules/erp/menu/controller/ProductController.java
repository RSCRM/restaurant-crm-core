package com.restaurant.crm.modules.erp.menu.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.ProductSearchRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateProductRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ProductResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ProductManagementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/products")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ProductController {

    ProductManagementService service;

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.PRODUCT_ADD + "')")
    public ResponseEntity<ApiResponse<ProductResponse>> create(@Valid @ModelAttribute CreateProductRequest request,
                                                               @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ProductResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.create(request, image)).build());
    }

    @PutMapping(value = "/{id}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<ProductResponse>> update(@PathVariable String id,
                                                               @Valid @ModelAttribute UpdateProductRequest request,
                                                               @RequestPart(value = "image", required = false) MultipartFile image) {
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.update(id, request, image)).build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.PRODUCT_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @PostMapping("/search")
    public ResponseEntity<ApiResponse<PagingResponse<ProductResponse>>> searchProducts(
            @RequestBody ProductSearchRequest searchRequest,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field
    ) {
        PagingRequest pagingRequest = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        ApiResponse<PagingResponse<ProductResponse>> response = ApiResponse.<PagingResponse<ProductResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(service.searchProducts(searchRequest, pagingRequest))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<ProductResponse>>> list(@RequestParam String branchId) {
        return ResponseEntity.ok(ApiResponse.<List<ProductResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listByBranch(branchId)).build());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ProductResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<ProductResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.get(id)).build());
    }
}
