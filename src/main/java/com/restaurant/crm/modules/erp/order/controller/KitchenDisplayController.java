package com.restaurant.crm.modules.erp.order.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.order.dto.response.KitchenOrderItemResponse;
import com.restaurant.crm.modules.erp.order.service.interfaces.KitchenDisplayService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Kitchen display board (uc-scf-01). The client polls this endpoint every ~2 seconds
 * to meet the "new items appear within 2s" criterion.
 *
 * TODO(security): restrict to the Chef role once kitchen roles exist in PredefinedRole.
 */
@RestController
@RequestMapping("/api/v1/kitchen")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class KitchenDisplayController {

    KitchenDisplayService kitchenDisplayService;

    @GetMapping("/order-items")
    public ResponseEntity<ApiResponse<List<KitchenOrderItemResponse>>> getKitchenQueue() {
        ApiResponse<List<KitchenOrderItemResponse>> response =
                ApiResponse.<List<KitchenOrderItemResponse>>builder()
                        .success(ApiConstant.SUCCESS)
                        .data(kitchenDisplayService.getKitchenQueue())
                        .build();

        return ResponseEntity.ok(response);
    }
}
