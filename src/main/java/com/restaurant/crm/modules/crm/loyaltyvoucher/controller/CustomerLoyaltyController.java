package com.restaurant.crm.modules.crm.loyaltyvoucher.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces.CustomerLoyaltyService;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Customer-facing loyalty endpoints (Voucher + Points).
 * Requires CUSTOMER_SESSION token — branchId, customerId, orderId are resolved from the session.
 */
@RestController
@RequestMapping("/api/v1/customer/loyalty")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
@PreAuthorize("hasRole('CUSTOMER_SESSION')")
public class CustomerLoyaltyController {

    CustomerLoyaltyService customerLoyaltyService;

    /** Xem điểm tích lũy hiện tại của khách hàng */
    @GetMapping("/points")
    public ResponseEntity<ApiResponse<CustomerPointResponse>> getMyPoints() {
        CustomerPointResponse response = customerLoyaltyService.getMyPoints();
        return ResponseEntity.ok(ApiResponse.<CustomerPointResponse>builder()
                .success(true)
                .data(response)
                .build());
    }

    /** Xem danh sách Voucher nhà hàng đang có (catalog) để đổi điểm */
    @GetMapping("/vouchers/catalog")
    public ResponseEntity<ApiResponse<List<CustomerVoucherApplicableResponse>>> getVoucherCatalog() {
        List<CustomerVoucherApplicableResponse> response = customerLoyaltyService.getVoucherCatalog();
        return ResponseEntity.ok(ApiResponse.<List<CustomerVoucherApplicableResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    /** Đổi điểm lấy Voucher (redeem) */
    @PostMapping("/vouchers/{voucherId}/redeem")
    public ResponseEntity<ApiResponse<String>> redeemVoucher(@PathVariable String voucherId) {
        String customerVoucherId = customerLoyaltyService.redeemVoucher(voucherId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data(customerVoucherId)
                .build());
    }

    /** Xem danh sách Voucher của tôi mà áp dụng được cho đơn hàng hiện tại */
    @GetMapping("/vouchers/applicable")
    public ResponseEntity<ApiResponse<List<CustomerVoucherApplicableResponse>>> getMyApplicableVouchers() {
        List<CustomerVoucherApplicableResponse> response = customerLoyaltyService.getMyApplicableVouchers();
        return ResponseEntity.ok(ApiResponse.<List<CustomerVoucherApplicableResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    /** Áp dụng Voucher vào đơn hàng hiện tại (tính giảm giá trực tiếp vào order) */
    @PostMapping("/vouchers/{customerVoucherId}/apply")
    public ResponseEntity<ApiResponse<String>> applyVoucher(@PathVariable String customerVoucherId) {
        customerLoyaltyService.applyVoucherToCurrentOrder(customerVoucherId);
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data("Đã áp dụng voucher thành công!")
                .build());
    }

    /** Hủy dùng Voucher (bỏ áp dụng Voucher đang dùng trên đơn hàng hiện tại) */
    @PostMapping("/vouchers/remove")
    public ResponseEntity<ApiResponse<String>> removeVoucher() {
        customerLoyaltyService.removeVoucherFromCurrentOrder();
        return ResponseEntity.ok(ApiResponse.<String>builder()
                .success(true)
                .data("Đã hủy dùng voucher!")
                .build());
    }
}
