package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, String> {
    Optional<Order> findOrderById(String orderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select orders from Order orders where orders.id = :orderId")
    Optional<Order> findByIdForUpdate(String orderId);

    boolean existsOrderById(String orderId);

    Optional<Order> findFirstByTableIdAndStatusOrderByCreatedAtDesc(String tableId, OrderStatus status);

    long countByAppliedVoucherId(String appliedVoucherId);

    long countByAppliedVoucherCodeIgnoreCase(String appliedVoucherCode);
}
