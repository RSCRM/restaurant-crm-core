package com.restaurant.crm.modules.crm.point_wallet.mapper;

import com.restaurant.crm.modules.crm.point_wallet.dto.response.CustomerPointHistoryResponse;
import com.restaurant.crm.modules.crm.point_wallet.dto.response.CustomerPointResponse;
import com.restaurant.crm.modules.crm.point_wallet.entity.CustomerPoint;
import com.restaurant.crm.modules.crm.point_wallet.entity.CustomerPointHistory;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface CustomerPointMapper {

    @Mapping(source = "customer.id", target = "customerId")
    CustomerPointResponse toCustomerPointResponse(CustomerPoint customerPoint);

    @Mapping(source = "customer.id", target = "customerId")
    CustomerPointHistoryResponse toCustomerPointHistoryResponse(CustomerPointHistory customerPointHistory);
}
