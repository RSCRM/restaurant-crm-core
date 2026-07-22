package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
}
