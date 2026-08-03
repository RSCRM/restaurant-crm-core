package com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces;

import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;

import java.util.List;

/**
 * Customer-facing loyalty service. Resolves customerId, branchId, orderId from the CUSTOMER_SESSION token.
 */
public interface CustomerLoyaltyService {

    /** Xem điểm tích lũy hiện tại */
    CustomerPointResponse getMyPoints();

    /** Xem danh sách Voucher nhà hàng đang có (catalog) để đổi điểm */
    List<CustomerVoucherApplicableResponse> getVoucherCatalog();

    /** Đổi điểm lấy Voucher */
    String redeemVoucher(String voucherId);

    /** Xem danh sách Voucher đã sở hữu mà áp dụng được cho đơn hiện tại */
    List<CustomerVoucherApplicableResponse> getMyApplicableVouchers();

    /** Áp dụng Voucher vào đơn hàng hiện tại */
    void applyVoucherToCurrentOrder(String customerVoucherId);

    /** Hủy áp dụng Voucher khỏi đơn hàng hiện tại */
    void removeVoucherFromCurrentOrder();
}
