package com.restaurant.crm.modules.crm.loyaltyvoucher.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.crm.modules.crm.loyaltyvoucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyaltyvoucher.enums.CustomerVoucherStatus;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, String> {
    Optional<CustomerVoucher> findByVoucherSn(String voucherSn);
    List<CustomerVoucher> findByCustomerIdAndRestaurantIdAndStatus(String customerId, String restaurantId, CustomerVoucherStatus status);
    Page<CustomerVoucher> findByCustomerIdAndRestaurantIdAndStatus(String customerId, String restaurantId, CustomerVoucherStatus status, Pageable pageable);
    Page<CustomerVoucher> findByCustomerIdAndRestaurantId(String customerId, String restaurantId, Pageable pageable);
    Optional<CustomerVoucher> findByOrderId(String orderId);
    List<CustomerVoucher> findByCustomerIdAndRestaurantId(String customerId, String restaurantId);
}
