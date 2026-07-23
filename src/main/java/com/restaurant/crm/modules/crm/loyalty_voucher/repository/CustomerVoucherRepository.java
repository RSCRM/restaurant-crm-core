package com.restaurant.crm.modules.crm.loyalty_voucher.repository;

import com.restaurant.crm.modules.crm.loyalty_voucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyalty_voucher.enums.CustomerVoucherStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, String> {
    Optional<CustomerVoucher> findByVoucherSn(String voucherSn);
    List<CustomerVoucher> findByCustomerIdAndRestaurantIdAndStatus(String customerId, String restaurantId, CustomerVoucherStatus status);
    Page<CustomerVoucher> findByCustomerIdAndRestaurantIdAndStatus(String customerId, String restaurantId, CustomerVoucherStatus status, Pageable pageable);
    Page<CustomerVoucher> findByCustomerIdAndRestaurantId(String customerId, String restaurantId, Pageable pageable);
}
