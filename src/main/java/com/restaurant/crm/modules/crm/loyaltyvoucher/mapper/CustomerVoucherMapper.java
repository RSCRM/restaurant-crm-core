package com.restaurant.crm.modules.crm.loyaltyvoucher.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;

@Mapper(componentModel = "spring", uses = {VoucherMapper.class})
public interface CustomerVoucherMapper {
    @Mapping(source = "customer.id", target = "customerId")
    CustomerVoucherResponse toCustomerVoucherResponse(CustomerVoucher customerVoucher);
}
