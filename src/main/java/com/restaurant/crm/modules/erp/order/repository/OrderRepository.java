package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findOrderById(String orderId);

    boolean existsOrderById(String orderId);

    Optional<Order> findFirstByTableIdAndStatusOrderByCreatedAtDesc(String tableId, OrderStatus status);
}
