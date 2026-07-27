package com.restaurant.crm.modules.crm.loyalty_voucher.mapper;

import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherCreationRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.request.VoucherUpdateRequest;
import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.entity.Voucher;
import org.mapstruct.Mapper;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface VoucherMapper {
    VoucherResponse toVoucherResponse(Voucher voucher);
    Voucher toVoucher(VoucherCreationRequest request);
    void updateVoucher(VoucherUpdateRequest request, @MappingTarget Voucher voucher);
}
