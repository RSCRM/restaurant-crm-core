package com.restaurant.crm.modules.crm.feedback.repository;

import com.restaurant.crm.modules.crm.feedback.entity.Feedback;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, String> {
    Optional<Feedback> findByOrderId(String orderId);
    Page<Feedback> findByRestaurantId(String restaurantId, Pageable pageable);
    Page<Feedback> findByCustomerId(String customerId, Pageable pageable);
}
