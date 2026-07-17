package com.restaurant.crm.modules.identity.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.constant.PaginationConstant;
import com.restaurant.crm.common.dto.request.PagingRequest;
import com.restaurant.crm.common.dto.request.SortRequest;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.identity.dto.request.RoleUpdateRequest;
import com.restaurant.crm.modules.identity.dto.response.RoleResponse;
import com.restaurant.crm.modules.identity.service.interfaces.RoleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/roles")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RoleController {

    RoleService roleService;

    @GetMapping
    public ResponseEntity<ApiResponse<PagingResponse<RoleResponse>>> getRoles(
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size,
            @RequestParam(required = false, defaultValue = PaginationConstant.DESC) String direction,
            @RequestParam(required = false, defaultValue = "createdAt") String field
    ) {
        PagingRequest request = PagingRequest.builder()
                .page(page)
                .pageSize(size)
                .sortRequest(SortRequest.builder()
                        .direction(direction)
                        .field(field)
                        .build())
                .build();

        ApiResponse<PagingResponse<RoleResponse>> response = ApiResponse.<PagingResponse<RoleResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(roleService.getRoles(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> getRoleById(@PathVariable String roleId) {
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(roleService.getById(roleId))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{roleId}")
    public ResponseEntity<ApiResponse<RoleResponse>> updateRole(
            @PathVariable String roleId,
            @Valid @RequestBody RoleUpdateRequest request
    ) {
        ApiResponse<RoleResponse> response = ApiResponse.<RoleResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(roleService.update(roleId, request))
                .build();

        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{roleId}")
    public ResponseEntity<ApiResponse<Void>> deleteRole(@PathVariable String roleId) {
        roleService.deleteById(roleId);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .build();

        return ResponseEntity.ok(response);
    }
}

