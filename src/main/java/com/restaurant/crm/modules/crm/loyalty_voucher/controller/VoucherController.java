package com.restaurant.crm.modules.crm.loyalty_voucher.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.service.interfaces.VoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/crm/vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class VoucherController {

    VoucherService voucherService;

    @PostMapping
    public ResponseEntity<ApiResponse<VoucherResponse>> createVoucher(@RequestBody @Valid VoucherCreationRequest request) {
        VoucherResponse response = voucherService.createVoucher(request);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<VoucherResponse>> updateVoucher(
            @PathVariable String id,
            @RequestBody @Valid VoucherUpdateRequest request
    ) {
        VoucherResponse response = voucherService.updateVoucher(id, request);
        return ResponseEntity.ok(ApiResponse.<VoucherResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/active")
    public ResponseEntity<ApiResponse<PagingResponse<VoucherResponse>>> getActiveVouchers(
            @RequestParam String restaurantId,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<VoucherResponse> response = voucherService.getActiveVouchersByRestaurant(restaurantId, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<VoucherResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}
