package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.constants.GroupCartControllerConstants;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartAddItemRequest;
import com.restaurant.crm.modules.erp.order.dto.request.GroupCartUpdateItemRequest;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartResponse;
import com.restaurant.crm.modules.erp.order.dto.response.GroupCartSubmitResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartService;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupCartSseService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;


@Tag(name = "Group Cart", description = "shared per-table cart and order submission")
@RestController
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('" + GroupCartControllerConstants.ROLE_CUSTOMER_SESSION + "')")
public class GroupCartController {

    GroupCartService groupCartService;
    GroupCartSseService groupCartSseService;

    @Operation(summary = "View the shared cart with live prices")
    @GetMapping(GroupCartControllerConstants.BASE)
    public ResponseEntity<ApiResponse<GroupCartResponse>> getCart() {

        return ok(groupCartService.getCart());
    }

    @Operation(summary = "Add an item to the shared cart")
    @PostMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_ITEMS)
    public ResponseEntity<ApiResponse<GroupCartResponse>> addItem(
            @Valid @RequestBody GroupCartAddItemRequest request) {
        return ok(groupCartService.addItem(request));
    }

    @Operation(summary = "Update a cart item (quantity/note/modifiers)")
    @PutMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_ITEM)
    public ResponseEntity<ApiResponse<GroupCartResponse>> updateItem(
            @PathVariable String cartItemId, @Valid @RequestBody GroupCartUpdateItemRequest request) {
        return ok(groupCartService.updateItem(cartItemId, request));
    }

    @Operation(summary = "Remove a cart item")
    @DeleteMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_ITEM)
    public ResponseEntity<ApiResponse<GroupCartResponse>> deleteItem(@PathVariable String cartItemId) {
        return ok(groupCartService.deleteItem(cartItemId));
    }

    @Operation(summary = "Hold the edit lock on a cart item")
    @PostMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_ITEM_LOCK)
    public ResponseEntity<ApiResponse<Void>> lockItem(@PathVariable String cartItemId) {
        groupCartService.lockItem(cartItemId);
        return ok(null);
    }

    @Operation(summary = "Release the edit lock on a cart item")
    @DeleteMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_ITEM_LOCK)
    public ResponseEntity<ApiResponse<Void>> unlockItem(@PathVariable String cartItemId) {
        groupCartService.unlockItem(cartItemId);
        return ok(null);
    }

    @Operation(summary = "OWNER only: submit the cart and send it to the kitchen")
    @PostMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_SUBMIT)
    public ResponseEntity<ApiResponse<GroupCartSubmitResponse>> submit() {

        return ok(groupCartService.submit());
    }

    @Operation(summary = "Subscribe to shared-cart updates over SSE")
    @GetMapping(GroupCartControllerConstants.BASE + GroupCartControllerConstants.PATH_SUBSCRIBE)
    public SseEmitter subscribe() {

        return groupCartSseService.createEmitter(AuthUtils.getSessionId());
    }

    private <T> ResponseEntity<ApiResponse<T>> ok(T data) {
        return ResponseEntity.ok(ApiResponse.<T>builder()
                .success(ApiConstant.SUCCESS)
                .data(data)
                .build());
    }
}
