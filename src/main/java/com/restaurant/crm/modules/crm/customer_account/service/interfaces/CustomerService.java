package com.restaurant.crm.modules.crm.customer_account.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.crm.customer_account.dto.request.CustomerIdentifyRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.response.CustomerResponse;

public interface CustomerService {
    CustomerResponse identifyAndInitializeWallet(CustomerIdentifyRequest request);
    CustomerResponse getCustomerById(String id);
    PagingResponse<CustomerResponse> getAllCustomers(int page, int size);
}
