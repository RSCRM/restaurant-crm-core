package com.restaurant.crm.modules.crm.loyalty_voucher.repository;

import com.restaurant.crm.modules.crm.loyalty_voucher.entity.CustomerVoucher;
import com.restaurant.crm.modules.crm.loyalty_voucher.enums.CustomerVoucherStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomerVoucherRepository extends JpaRepository<CustomerVoucher, String> {
    Optional<CustomerVoucher> findByVoucherSn(String voucherSn);
    List<CustomerVoucher> findByCustomerIdAndRestaurantIdAndStatus(String customerId, String restaurantId, CustomerVoucherStatus status);
}
