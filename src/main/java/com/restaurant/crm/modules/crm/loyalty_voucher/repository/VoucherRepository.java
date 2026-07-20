package com.restaurant.crm.modules.crm.loyalty_voucher.repository;

import com.restaurant.crm.modules.crm.loyalty_voucher.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface VoucherRepository extends JpaRepository<Voucher, String> {
    List<Voucher> findByRestaurantIdAndIsActive(String restaurantId, Short isActive);
}
