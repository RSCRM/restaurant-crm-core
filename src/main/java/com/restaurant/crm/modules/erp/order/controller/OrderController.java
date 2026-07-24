package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.dto.request.CreateOrderRequestDto;
import com.restaurant.crm.modules.erp.order.dto.response.CancelOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.CreateOrderResponse;
import com.restaurant.crm.modules.erp.order.dto.response.OrderCookingStatusResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSseService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OrderService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderController {

    OrderService orderService;
    CustomerSseService customerSseService;

    @PostMapping
    public ResponseEntity<ApiResponse<CreateOrderResponse>> createOrder(
            @Valid @RequestBody CreateOrderRequestDto request
    ) {
        ApiResponse<CreateOrderResponse> response = ApiResponse.<CreateOrderResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(orderService.create(request))
                .build();

        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{orderId}/cancel")
    public ResponseEntity<ApiResponse<CancelOrderResponse>> cancelOrder(@PathVariable String orderId) {
        ApiResponse<CancelOrderResponse> response = ApiResponse.<CancelOrderResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(orderService.cancelOrder(orderId))
                .build();

        return ResponseEntity.ok(response);
    }

    @GetMapping("/{orderId}/cooking-status")
    public ResponseEntity<ApiResponse<OrderCookingStatusResponse>> getOrderCookingStatus(
            @PathVariable String orderId
    ) {
        OrderCookingStatusResponse cookingStatus = orderService.getOrderCookingStatus(orderId);
        ApiResponse<OrderCookingStatusResponse> response = ApiResponse.<OrderCookingStatusResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(cookingStatus)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tables/{tableId}/active-order/cooking-status")
    public ResponseEntity<ApiResponse<OrderCookingStatusResponse>> getActiveOrderCookingStatusByTable(
            @PathVariable String tableId
    ) {
        OrderCookingStatusResponse cookingStatus = orderService.getActiveOrderCookingStatusByTable(tableId);
        ApiResponse<OrderCookingStatusResponse> response = ApiResponse.<OrderCookingStatusResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(cookingStatus)
                .build();
        return ResponseEntity.ok(response);
    }

    @GetMapping(value = "/{orderId}/cooking-status/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public SseEmitter subscribeCookingStatus(@PathVariable String orderId) {
        return customerSseService.createEmitter(orderId);
    }
}
