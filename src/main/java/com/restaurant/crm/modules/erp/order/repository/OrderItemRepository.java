package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            where oi.id = :orderItemId
              and o.id = :orderId
            """)
    Optional<OrderItem> findByIdAndOrderIdWithOrder(String orderItemId, String orderId);
    List<OrderItem> findByOrderId(String orderId);
}
