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

/**
 * Controller class exposing REST endpoints for managing order items.
 */
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemController {

    OrderItemService orderItemService;

    /**
     * Updates the status of an order item.
     * Accessible by employees with ORDER_UPDATE permission.
     *
     * @param id the ID of the order item to update
     * @param request the request body containing the new status
     * @return the ApiResponse containing the updated order item details
     */
    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).ORDER_UPDATE)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> updateStatus(
            @PathVariable String id,
            @Valid @RequestBody UpdateOrderItemStatusRequest request
    ) {
        OrderItemResponse updatedItem = orderItemService.updateStatus(id, request.getStatus());
        ApiResponse<OrderItemResponse> response = ApiResponse.<OrderItemResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(updatedItem)
                .build();
        return ResponseEntity.ok(response);
    }
}
