package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItemModifier;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderItemModifierRepository extends JpaRepository<OrderItemModifier, String> {
    void deleteByOrderItemId(String orderItemId);
}
