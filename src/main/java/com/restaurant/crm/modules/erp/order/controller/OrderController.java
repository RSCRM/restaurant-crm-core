package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.dto.request.AddOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.request.ModifyOrderItemRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.AddOrderItemResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;

    @PostMapping("/{orderId}/items")
    public ResponseEntity<ApiResponse<AddOrderItemResponse>> addOrderItem(
            @PathVariable String orderId,
            @Valid @RequestBody AddOrderItemRequestDto request
    ) {
        ApiResponse<AddOrderItemResponse> response = ApiResponse.<AddOrderItemResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(orderService.addOrderItem(orderId, request))
                .build();

        return ResponseEntity.ok(response);
    }

    @PutMapping("/{orderId}/items/{orderItemId}")
    public ResponseEntity<ApiResponse<Void>> modifyOrderItem(
            @PathVariable String orderId,
            @PathVariable String orderItemId,
            @Valid @RequestBody ModifyOrderItemRequestDto request
    ) {
        orderService.modifyOrderItem(orderId, orderItemId, request);

        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .build();

        return ResponseEntity.ok(response);
    }
}
