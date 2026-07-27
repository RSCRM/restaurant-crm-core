package com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherRedeemRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherApplicableResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherResponse;

import java.math.BigDecimal;
import java.util.List;

public interface CustomerVoucherService {
    CustomerVoucherResponse redeemVoucher(VoucherRedeemRequest request);
    CustomerVoucherResponse giveVoucherDirectly(String customerId, String restaurantId, String voucherId);
    CustomerVoucherResponse useVoucher(String customerVoucherId, String orderId, BigDecimal billAmount);
    PagingResponse<CustomerVoucherResponse> getCustomerVouchers(String customerId, String restaurantId, String status, int page, int size);
    List<CustomerVoucherApplicableResponse> getApplicableVouchers(String customerId, String restaurantId, BigDecimal subtotal);
    void releaseVoucher(String orderId);
}
