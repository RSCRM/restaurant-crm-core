package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    java.util.List<OrderItem> findByOrderId(String orderId);
}
