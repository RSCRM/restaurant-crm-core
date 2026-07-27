package com.restaurant.crm.modules.crm.loyalty_voucher.mapper;

import com.restaurant.crm.modules.crm.loyalty_voucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyalty_voucher.entity.CustomerVoucher;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = {VoucherMapper.class})
public interface CustomerVoucherMapper {

    @Mapping(source = "customer.id", target = "customerId")
    CustomerVoucherResponse toCustomerVoucherResponse(CustomerVoucher customerVoucher);
}
