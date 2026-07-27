package com.restaurant.crm.modules.crm.pointwallet.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.pointwallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPoint;
import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPointHistory;

@Mapper(componentModel = "spring")
public interface CustomerPointMapper {

    @Mapping(source = "customer.id", target = "customerId")
    CustomerPointResponse toCustomerPointResponse(CustomerPoint customerPoint);

    @Mapping(source = "customer.id", target = "customerId")
    CustomerPointHistoryResponse toCustomerPointHistoryResponse(CustomerPointHistory customerPointHistory);
}
