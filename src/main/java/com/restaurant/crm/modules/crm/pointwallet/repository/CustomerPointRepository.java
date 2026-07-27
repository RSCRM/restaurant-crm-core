package com.restaurant.crm.modules.crm.pointwallet.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.restaurant.crm.modules.crm.pointwallet.entity.CustomerPoint;

import java.util.Optional;

@Repository
public interface CustomerPointRepository extends JpaRepository<CustomerPoint, String> {
    Optional<CustomerPoint> findByCustomerIdAndRestaurantId(String customerId, String restaurantId);
}
