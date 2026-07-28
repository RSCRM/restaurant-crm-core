package com.restaurant.crm.modules.crm;

import com.restaurant.crm.modules.crm.customeraccount.dto.request.CustomerIdentifyRequest;
import com.restaurant.crm.modules.crm.customeraccount.dto.response.CustomerResponse;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.crm.customeraccount.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customeraccount.mapper.CustomerMapper;
import com.restaurant.crm.modules.crm.customeraccount.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customeraccount.service.impl.CustomerServiceImpl;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.request.VoucherRedeemRequest;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.CustomerVoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.dto.response.VoucherResponse;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.Voucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;
import com.restaurant.crm.modules.crm.loyaltyvoucher.mapper.CustomerVoucherMapper;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.CustomerVoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.repository.VoucherRepository;
import com.restaurant.crm.modules.crm.loyaltyvoucher.service.impl.CustomerVoucherServiceImpl;
import com.restaurant.crm.modules.crm.pointwallet.service.interfaces.PointWalletService;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CustomerLoyaltyTests {

    @Mock
    CustomerRepository customerRepository;
    @Mock
    CustomerMapper customerMapper;
    @Mock
    PointWalletService pointWalletService;

    @InjectMocks
    CustomerServiceImpl customerService;

    @Mock
    CustomerVoucherRepository customerVoucherRepository;
    @Mock
    VoucherRepository voucherRepository;
    @Mock
    CustomerVoucherMapper customerVoucherMapper;

    @InjectMocks
    CustomerVoucherServiceImpl customerVoucherService;

    @Test
    public void testIdentifyCustomer_NewCustomer() {
        CustomerIdentifyRequest request = CustomerIdentifyRequest.builder()
                .phone("0987654321")
                .restaurantId("rest-1")
                .build();

        when(customerRepository.findByPhone(request.getPhone())).thenReturn(Optional.empty());
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse expectedResponse = CustomerResponse.builder()
                .phone("0987654321")
                .status(CustomerStatus.ACTIVE)
                .build();
        when(customerMapper.toCustomerResponse(any(Customer.class))).thenReturn(expectedResponse);

        CustomerResponse response = customerService.identifyAndInitializeWallet(request);

        assertNotNull(response);
        assertEquals("0987654321", response.getPhone());
        verify(pointWalletService, times(1)).initializeWallet(any(), eq("rest-1"));
    }

    @Test
    public void testRedeemVoucher_Success() {
        VoucherRedeemRequest request = VoucherRedeemRequest.builder()
                .customerId("cust-1")
                .restaurantId("rest-1")
                .voucherId("vouch-1")
                .build();

        Voucher voucher = Voucher.builder()
                .id("vouch-1")
                .restaurantId("rest-1")
                .title("Discount 10%")
                .discountPercent(10)
                .minBillAmount(BigDecimal.valueOf(100))
                .pointsRequired(100)
                .isActive((short) 1)
                .build();

        Customer customer = Customer.builder()
                .id("cust-1")
                .phone("0987654321")
                .status(CustomerStatus.ACTIVE)
                .build();

        when(voucherRepository.findById("vouch-1")).thenReturn(Optional.of(voucher));
        when(customerRepository.findById("cust-1")).thenReturn(Optional.of(customer));
        when(customerVoucherRepository.findByVoucherSn(anyString())).thenReturn(Optional.empty());
        when(customerVoucherRepository.save(any(CustomerVoucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoucherResponse voucherResponse = VoucherResponse.builder()
                .id("vouch-1")
                .title("Discount 10%")
                .discountPercent(10)
                .build();
        CustomerVoucherResponse expectedResponse = CustomerVoucherResponse.builder()
                .customerId("cust-1")
                .restaurantId("rest-1")
                .voucher(voucherResponse)
                .status(CustomerVoucherStatus.AVAILABLE)
                .build();
        when(customerVoucherMapper.toCustomerVoucherResponse(any(CustomerVoucher.class))).thenReturn(expectedResponse);

        CustomerVoucherResponse response = customerVoucherService.redeemVoucher(request);

        assertNotNull(response);
        assertEquals("cust-1", response.getCustomerId());
        assertEquals(CustomerVoucherStatus.AVAILABLE, response.getStatus());
        verify(pointWalletService, times(1)).deductPoints(eq("cust-1"), eq("rest-1"), eq(100), anyString());
    }
}
