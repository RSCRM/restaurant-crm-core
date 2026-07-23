package com.restaurant.crm.modules.crm.customer_account.mapper;

import com.restaurant.crm.modules.crm.customer_account.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CustomerMapper {
    CustomerResponse toCustomerResponse(Customer customer);
}
