package com.restaurant.crm.modules.crm.loyalty_voucher.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherRedeemRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherResponse;

import java.math.BigDecimal;

public interface CustomerVoucherService {
    CustomerVoucherResponse redeemVoucher(VoucherRedeemRequest request);
    CustomerVoucherResponse giveVoucherDirectly(String customerId, String restaurantId, String voucherId);
    CustomerVoucherResponse useVoucher(String customerVoucherId, String orderId, BigDecimal billAmount);
    PagingResponse<CustomerVoucherResponse> getCustomerVouchers(String customerId, String restaurantId, String status, int page, int size);
}
