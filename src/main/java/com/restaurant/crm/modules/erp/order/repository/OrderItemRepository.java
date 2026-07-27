package com.restaurant.crm.modules.erp.order.repository;

import com.restaurant.crm.modules.erp.order.entity.OrderItem;
import com.restaurant.crm.modules.erp.order.enums.OrderItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
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

    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            where o.branchId = :branchId
              and oi.status in :statuses
            order by oi.createdAt asc
            """)
    List<OrderItem> findByBranchIdAndStatusIn(
            @Param("branchId") String branchId,
            @Param("statuses") List<OrderItemStatus> statuses
    );

    @Query("""
            select oi
            from OrderItem oi
            join fetch oi.order o
            where o.branchId = :branchId
              and oi.status in :statuses
              and oi.updatedAt >= :since
            order by oi.updatedAt desc
            """)
    List<OrderItem> findByBranchIdAndStatusInAndUpdatedAtAfter(
            @Param("branchId") String branchId,
            @Param("statuses") List<OrderItemStatus> statuses,
            @Param("since") Instant since
    );
}

