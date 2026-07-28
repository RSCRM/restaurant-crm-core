package com.restaurant.crm.modules.erp.menu.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.menu.constants.permission.MenuPermissionConstants;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.CreateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierGroupRequest;
import com.restaurant.crm.modules.erp.menu.dto.request.UpdateModifierOptionRequest;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierGroupResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.ModifierOptionResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.ModifierManagementService;
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
public class ModifierController {

    ModifierManagementService service;

    @PostMapping("/products/{productId}/modifier-groups")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<ModifierGroupResponse>> createGroup(@PathVariable String productId, @Valid @RequestBody CreateModifierGroupRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ModifierGroupResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.createGroup(productId, request)).build());
    }

    @GetMapping("/products/{productId}/modifier-groups")
    public ResponseEntity<ApiResponse<List<ModifierGroupResponse>>> listGroups(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.<List<ModifierGroupResponse>>builder()
                .success(ApiConstant.SUCCESS).data(service.listGroupsByProduct(productId)).build());
    }

    @PutMapping("/modifier-groups/{groupId}")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<ModifierGroupResponse>> updateGroup(@PathVariable String groupId, @Valid @RequestBody UpdateModifierGroupRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModifierGroupResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.updateGroup(groupId, request)).build());
    }

    @DeleteMapping("/modifier-groups/{groupId}")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteGroup(@PathVariable String groupId) {
        service.deleteGroup(groupId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @PostMapping("/modifier-groups/{groupId}/options")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<ModifierOptionResponse>> createOption(@PathVariable String groupId, @Valid @RequestBody CreateModifierOptionRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<ModifierOptionResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.createOption(groupId, request)).build());
    }

    @PutMapping("/modifier-options/{optionId}")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<ModifierOptionResponse>> updateOption(@PathVariable String optionId, @Valid @RequestBody UpdateModifierOptionRequest request) {
        return ResponseEntity.ok(ApiResponse.<ModifierOptionResponse>builder()
                .success(ApiConstant.SUCCESS).data(service.updateOption(optionId, request)).build());
    }

    @DeleteMapping("/modifier-options/{optionId}")
    @PreAuthorize("@menuAccessChecker.canManage('" + MenuPermissionConstants.PRODUCT_UPDATE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteOption(@PathVariable String optionId) {
        service.deleteOption(optionId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }
}
