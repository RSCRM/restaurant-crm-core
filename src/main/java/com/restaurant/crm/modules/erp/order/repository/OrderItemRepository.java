package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            where oi.id = :orderItemId
            """)
    Optional<OrderItem> findByIdWithOrder(String orderItemId);
}
