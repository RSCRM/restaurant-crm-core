package com.restaurant.crm.modules.erp.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.service.interfaces.KitchenDisplayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kitchen display board (uc-scf-01). The front end polls this endpoint every ~2 seconds
 * to satisfy the "new items appear within 2s" criterion without WebSocket.
 *
 * TODO(uc-scf-01): {@code branchId} is taken as a request param until the identity module
 * (NhatNL11) exposes the current user's branch. Once available, resolve it server-side and
 * drop the param so a caller cannot read another branch's queue (NFR-07).
 * TODO(security): restrict to the Chef role once kitchen roles are added to PredefinedRole.
 */
@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KitchenDisplayController {

    KitchenDisplayService kitchenDisplayService;

    @GetMapping("/order-items")
    public ResponseEntity<ApiResponse<List<KitchenOrderItemResponse>>> getKitchenQueue(
            @RequestParam("branchId") String branchId
    ) {
        ApiResponse<List<KitchenOrderItemResponse>> response =
                ApiResponse.<List<KitchenOrderItemResponse>>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(kitchenDisplayService.getKitchenQueue(branchId))
                        .build();

        return ResponseEntity.ok(response);
    }
}
