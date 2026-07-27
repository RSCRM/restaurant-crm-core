package com.restaurant.crm.modules.crm.customeraccount.mapper;

import org.mapstruct.Mapper;

import com.restaurant.crm.modules.crm.customeraccount.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toCustomerResponse(Customer customer);
}
