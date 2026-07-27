package com.restaurant.crm.modules.crm.pointwallet.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crm/wallets")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PointWalletController {

    PointWalletService pointWalletService;

    @GetMapping("/balance")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> getBalance(
            @RequestParam String customerId,
            @RequestParam String restaurantId
    ) {
        CustomerPointResponse response = pointWalletService.getWallet(customerId, restaurantId);
        return ResponseEntity.ok(ApiResponse.<CustomerPointResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/history")
    public ResponseEntity<ApiResponse<PagingResponse<CustomerPointHistoryResponse>>> getHistory(
            @RequestParam String customerId,
            @RequestParam String restaurantId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<CustomerPointHistoryResponse> response = pointWalletService.getHistory(customerId, restaurantId, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<CustomerPointHistoryResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}
