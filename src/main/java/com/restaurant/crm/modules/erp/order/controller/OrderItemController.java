package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.dto.request.UpdateOrderItemStatusRequest;
import com.restaurant.crm.modules.erp.order.dto.response.OrderItemResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderItemService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemController {

    OrderItemService orderItemService;


    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_UPDATE, T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_READ) || hasRole('CUSTOMER_SESSION')")
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderItemStatusRequest request
    ) {
        OrderItemResponse updatedItem = orderItemService.updateStatus(id, request);
        ApiResponse<OrderItemResponse> response = ApiResponse.<OrderItemResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(updatedItem)
                .build();
        return ResponseEntity.ok(response);
    }
}
