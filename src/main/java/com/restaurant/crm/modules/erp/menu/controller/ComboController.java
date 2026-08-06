package com.restaurant.crm.modules.erp.menu.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
import com.restaurant.crm.modules.erp.menu.dto.request.ComboItemRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.ComboSearchRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateComboRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboItemResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ComboResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ComboManagementService;
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
@RequestMapping("/api/v1/erp")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ComboController {

    ComboManagementService service;

    @PostMapping("/combos")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_ADD + "')")
    public ResponseEntity<ApiResponse<ComboResponse>> create(@Valid @RequestBody CreateComboRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ComboResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.create(request)).build());
    }

    @PutMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_UPDATE + "')")
    public ResponseEntity<ApiResponse<ComboResponse>> update(@PathVariable String id, @Valid @RequestBody UpdateComboRequest request) {
        return ResponseEntity.ok(ApiResponse.<ComboResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.update(id, request)).build());
    }

    @DeleteMapping("/combos/{id}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable String id) {
        service.delete(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @PostMapping("/combos/search")
    public ResponseEntity<ApiResponse<PagingResponse<ComboResponse>>> searchCombos(
            @RequestBody ComboSearchRequest searchRequest,
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

        ApiResponse<PagingResponse<ComboResponse>> response = ApiResponse.<PagingResponse<ComboResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(service.searchCombos(searchRequest, pagingRequest))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/combos")
    public ResponseEntity<ApiResponse<List<ComboResponse>>> list(@RequestParam String branchId) {
        return ResponseEntity.ok(ApiResponse.<List<ComboResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listByBranch(branchId)).build());
    }

    @GetMapping("/combos/{id}")
    public ResponseEntity<ApiResponse<ComboResponse>> get(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<ComboResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.get(id)).build());
    }

    @PostMapping("/combos/{comboId}/items")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_UPDATE + "')")
    public ResponseEntity<ApiResponse<ComboItemResponse>> addItem(@PathVariable String comboId, @Valid @RequestBody ComboItemRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ComboItemResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.addItem(comboId, request)).build());
    }

    @PutMapping("/combo-items/{itemId}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_UPDATE + "')")
    public ResponseEntity<ApiResponse<ComboItemResponse>> updateItem(@PathVariable String itemId, @Valid @RequestBody ComboItemRequest request) {
        return ResponseEntity.ok(ApiResponse.<ComboItemResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.updateItem(itemId, request)).build());
    }

    @DeleteMapping("/combo-items/{itemId}")
    @PreAuthorize("hasAuthority('" + MenuPermissionConstants.COMBO_UPDATE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable String itemId) {
        service.deleteItem(itemId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }
}
