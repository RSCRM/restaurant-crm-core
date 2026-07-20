package com.restaurant.crm.modules.crm.point_wallet.repository;

import com.restaurant.crm.modules.crm.point_wallet.entity.CustomerPointHistory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CustomerPointHistoryRepository extends JpaRepository<CustomerPointHistory, String> {
    Page<CustomerPointHistory> findByCustomerIdAndRestaurantId(String customerId, String restaurantId, Pageable pageable);
}
