package com.restaurant.crm.modules.crm.loyalty_voucher.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherCreationRequest request);
    VoucherResponse updateVoucher(String id, VoucherUpdateRequest request);
    PagingResponse<VoucherResponse> getActiveVouchersByRestaurant(String restaurantId, int page, int size);
}
