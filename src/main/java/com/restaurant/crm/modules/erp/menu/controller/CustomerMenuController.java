package com.restaurant.crm.modules.erp.menu.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.menu.constants.CustomerMenuControllerConstants;
import com.restaurant.crm.modules.erp.menu.dto.response.CustomerMenuResponse;
import com.restaurant.crm.modules.erp.menu.dto.response.MenuProductResponse;
import com.restaurant.crm.modules.erp.menu.service.interfaces.CustomerMenuService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * Customer digital-menu endpoints (uc-c-04). Requires a CUSTOMER_SESSION token; the branch is
 * always resolved from that token (NFR-07), never from the request.
 */
@Tag(name = "Customer Menu", description = "uc-c-04 — browse the branch digital menu")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerMenuController {

    CustomerMenuService customerMenuService;

    @Operation(summary = "Get the full menu of the caller's branch (CUSTOMER_SESSION token)")
    @GetMapping(CustomerMenuControllerConstants.BASE)
    @PreAuthorize("hasRole('" + CustomerMenuControllerConstants.ROLE_CUSTOMER_SESSION + "')")
    public ResponseEntity<ApiResponse<CustomerMenuResponse>> getMenu() {
        return ResponseEntity.ok(ApiResponse.<CustomerMenuResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(customerMenuService.getMenu())
                .build());
    }

    @Operation(summary = "Get one product of the caller's branch (CUSTOMER_SESSION token)")
    @GetMapping(CustomerMenuControllerConstants.BASE + CustomerMenuControllerConstants.PATH_PRODUCT)
    @PreAuthorize("hasRole('" + CustomerMenuControllerConstants.ROLE_CUSTOMER_SESSION + "')")
    public ResponseEntity<ApiResponse<MenuProductResponse>> getProduct(@PathVariable String productId) {
        return ResponseEntity.ok(ApiResponse.<MenuProductResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(customerMenuService.getProduct(productId))
                .build());
    }
}
