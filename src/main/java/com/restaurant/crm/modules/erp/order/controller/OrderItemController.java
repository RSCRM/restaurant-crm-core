package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.dto.request.OrderItemCancelRequest;
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
 * The order-item status flow is split per action so each transition carries its own permission.
 */
@RestController
@RequestMapping("/api/v1/order-items")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OrderItemController {

    OrderItemService orderItemService;

    /** Kitchen accepts an item for preparation (PENDING -> IN_PROGRESS). */
    @PatchMapping("/{id}/accept")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).KITCHEN_ITEM_ACCEPT)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> accept(
            @PathVariable String id
    ) {
        return ok(orderItemService.accept(id));
    }

    /** Kitchen marks preparation done (IN_PROGRESS -> READY_TO_SERVE). */
    @PatchMapping("/{id}/complete")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).KITCHEN_ITEM_UPDATE)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> complete(
            @PathVariable String id
    ) {
        return ok(orderItemService.complete(id));
    }

    /** Kitchen releases an accepted item back to the queue (IN_PROGRESS -> PENDING). */
    @PatchMapping("/{id}/release")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).KITCHEN_ITEM_RELEASE)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> release(
            @PathVariable String id
    ) {
        return ok(orderItemService.release(id));
    }

    /** Kitchen cancels an item, reason required (PENDING/IN_PROGRESS -> CANCELLED). */
    @PatchMapping("/{id}/cancel")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).KITCHEN_ITEM_CANCEL)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> cancel(
            @PathVariable String id,
            @Valid @RequestBody OrderItemCancelRequest request
    ) {
        return ok(orderItemService.cancel(id, request.getReason()));
    }

    /** Service marks a no-preparation item ready (PENDING -> READY_TO_SERVE). */
    @PatchMapping("/{id}/ready")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).SERVICE_ITEM_READY)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> markReady(
            @PathVariable String id
    ) {
        return ok(orderItemService.markReady(id));
    }

    /** Service delivers the item to the table (READY_TO_SERVE -> SERVED). */
    @PatchMapping("/{id}/serve")
    @PreAuthorize("hasAuthority(T(com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission).SERVICE_ITEM_SERVE)")
    public ResponseEntity<ApiResponse<OrderItemResponse>> serve(
            @PathVariable String id
    ) {
        return ok(orderItemService.serve(id));
    }

    private ResponseEntity<ApiResponse<OrderItemResponse>> ok(OrderItemResponse item) {
        return ResponseEntity.ok(ApiResponse.<OrderItemResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(item)
                .build());
    }
}
