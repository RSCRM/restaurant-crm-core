package com.restaurant.crm.modules.crm.loyaltyvoucher.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    VoucherResponse toVoucherResponse(Voucher voucher);
    Voucher toVoucher(VoucherCreationRequest request);
    void updateVoucher(VoucherUpdateRequest request, @MappingTarget Voucher voucher);
}
