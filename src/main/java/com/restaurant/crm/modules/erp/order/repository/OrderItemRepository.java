package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    Optional<OrderItem> findByIdAndOrderId(String orderItemId, String orderId);

    List<OrderItem> findAllByOrderId(String orderId);
}
