package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {
    java.util.List<OrderItem> findByOrderId(String orderId);

    /**
     * Kitchen FIFO queue for one branch (uc-scf-01).
     * Isolated by branch for NFR-07 and ordered per BR-RES-ORD-04:
     * priority items first, then oldest first.
     */
    @Query("""
            SELECT oi FROM OrderItem oi
            JOIN oi.order o
            WHERE oi.status IN :statuses
              AND o.branchId = :branchId
            ORDER BY oi.priorityFlag DESC, oi.createdAt ASC
            """)
    List<OrderItem> findKitchenQueue(
            @Param("branchId") String branchId,
            @Param("statuses") Collection<OrderItemStatus> statuses
    );
}
