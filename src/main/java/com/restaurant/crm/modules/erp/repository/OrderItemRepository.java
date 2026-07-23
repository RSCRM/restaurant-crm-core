package com.restaurant.crm.modules.erp.repository;

import com.restaurant.crm.modules.erp.entity.OrderItem;
import com.restaurant.crm.modules.erp.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Collection;
import java.util.List;

@Repository
public interface OrderItemRepository extends JpaRepository<OrderItem, String> {

    /**
     * Kitchen FIFO queue for a branch (uc-scf-01).
     * Filters by branch for NFR-07 isolation and orders per BR-RES-ORD-04
     * (priority first, then oldest first). See state-machine doc section 4.
     */
    @Query("""
            SELECT oi FROM OrderItem oi
            WHERE oi.status IN :statuses
              AND oi.order.branchId = :branchId
            ORDER BY oi.priorityFlag DESC, oi.createdAt ASC
            """)
    List<OrderItem> findKitchenQueue(
            @Param("branchId") String branchId,
            @Param("statuses") Collection<OrderItemStatus> statuses
    );
}
