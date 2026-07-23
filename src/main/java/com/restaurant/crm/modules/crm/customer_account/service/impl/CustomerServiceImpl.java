package com.restaurant.crm.modules.crm.customer_account.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.dto.request.CustomerIdentifyRequest;
import com.restaurant.crm.modules.crm.customer_account.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customer_account.mapper.CustomerMapper;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.CustomerService;
import com.restaurant.crm.modules.crm.point_wallet.service.interfaces.PointWalletService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerServiceImpl implements CustomerService {

    CustomerRepository customerRepository;
    CustomerMapper customerMapper;
    PointWalletService pointWalletService;

    @Override
    @Transactional
    public CustomerResponse identifyAndInitializeWallet(CustomerIdentifyRequest request) {
        // 1. find or create Customer
        Customer customer = customerRepository.findByPhone(request.getPhone())
                .orElseGet(() -> {
                    Customer newCustomer = Customer.builder()
                            .phone(request.getPhone())
                            .status(CustomerStatus.ACTIVE)
                            .build();
                    return customerRepository.save(newCustomer);
                });

        // check account status
        if (customer.getStatus() == CustomerStatus.LOCKED) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }

        // 2. initialize wallet for the customer(for the restaurant)
        pointWalletService.initializeWallet(customer.getId(), request.getRestaurantId());

        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public CustomerResponse getCustomerById(String id) {
        Customer customer = customerRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.CUSTOMER_NOT_FOUND));
        return customerMapper.toCustomerResponse(customer);
    }

    @Override
    public PagingResponse<CustomerResponse> getAllCustomers(int page, int size) {
        int adjustedPage = Math.max(0, page - 1);
        Pageable pageable = PageRequest.of(adjustedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Customer> customerPage = customerRepository.findAll(pageable);

        return PagingResponse.<CustomerResponse>builder()
                .currentPage(page)
                .pageSize(size)
                .totalPages(customerPage.getTotalPages())
                .totalElement(customerPage.getTotalElements())
                .data(customerPage.getContent().stream().map(customerMapper::toCustomerResponse).toList())
                .build();
    }
}
