package com.restaurant.crm.modules.crm.point_wallet.repository;

import com.restaurant.crm.modules.crm.point_wallet.entity.CustomerPoint;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CustomerPointRepository extends JpaRepository<CustomerPoint, String> {
    Optional<CustomerPoint> findByCustomerIdAndRestaurantId(String customerId, String restaurantId);
}
