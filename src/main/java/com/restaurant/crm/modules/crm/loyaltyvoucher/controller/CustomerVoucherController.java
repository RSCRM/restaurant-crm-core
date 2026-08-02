package com.restaurant.crm.modules.crm.loyaltyvoucher.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherRedeemRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.CustomerVoucherService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/crm/customer-vouchers")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerVoucherController {

    CustomerVoucherService customerVoucherService;

    @PostMapping("/redeem")
    @PreAuthorize("hasAuthority('CUSTOMER_VOUCHER_REDEEM')")
    public ResponseEntity<ApiResponse<CustomerVoucherResponse>> redeemVoucher(@RequestBody @Valid VoucherRedeemRequest request) {
        CustomerVoucherResponse response = customerVoucherService.redeemVoucher(request);
        return ResponseEntity.ok(ApiResponse.<CustomerVoucherResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/give")
    @PreAuthorize("hasAuthority('CUSTOMER_VOUCHER_GIVE')")
    public ResponseEntity<ApiResponse<CustomerVoucherResponse>> giveVoucher(
            @RequestParam String customerId,
            @RequestParam String branchId,
            @RequestParam String voucherId
    ) {
        CustomerVoucherResponse response = customerVoucherService.giveVoucherDirectly(customerId, branchId, voucherId);
        return ResponseEntity.ok(ApiResponse.<CustomerVoucherResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @PostMapping("/use/{id}")
    @PreAuthorize("hasAuthority('CUSTOMER_VOUCHER_USE')")
    public ResponseEntity<ApiResponse<CustomerVoucherResponse>> useVoucher(
            @PathVariable String id,
            @RequestParam String orderId,
            @RequestParam BigDecimal billAmount
    ) {
        CustomerVoucherResponse response = customerVoucherService.useVoucher(id, orderId, billAmount);
        return ResponseEntity.ok(ApiResponse.<CustomerVoucherResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping
    @PreAuthorize("hasAuthority('CUSTOMER_VOUCHER_READ')")
    public ResponseEntity<ApiResponse<PagingResponse<CustomerVoucherResponse>>> getCustomerVouchers(
            @RequestParam String customerId,
            @RequestParam String branchId,
            @RequestParam(required = false) String status,
            @RequestParam(value = "page", required = false, defaultValue = "1") int page,
            @RequestParam(value = "size", required = false, defaultValue = "10") int size
    ) {
        PagingResponse<CustomerVoucherResponse> response = customerVoucherService.getCustomerVouchers(customerId, branchId, status, page, size);
        return ResponseEntity.ok(ApiResponse.<PagingResponse<CustomerVoucherResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}
