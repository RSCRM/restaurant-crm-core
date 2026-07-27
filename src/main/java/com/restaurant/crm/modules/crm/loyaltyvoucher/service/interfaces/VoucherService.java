package com.restaurant.crm.modules.crm.loyaltyvoucher.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.VoucherResponse;

public interface VoucherService {
    VoucherResponse createVoucher(VoucherCreationRequest request);
    VoucherResponse updateVoucher(String id, VoucherUpdateRequest request);
    PagingResponse<VoucherResponse> getActiveVouchersByRestaurant(String restaurantId, int page, int size);
}
