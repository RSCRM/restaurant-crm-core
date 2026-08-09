package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface InventoryRepository extends
    JpaRepository<Inventory, String>,
    JpaSpecificationExecutor<Inventory> {

    Optional<Inventory> findByIdAndBranchId(
        String id,
        String branchId
    );

    Page<Inventory> findByBranchId(
        String branchId,
        Pageable pageable
    );

    Page<Inventory> findByInventoryCategoryIdAndBranchId(
        String inventoryCategoryId,
        String branchId,
        Pageable pageable
    );

    boolean existsByBranchIdAndInventoryName(
        String branchId,
        String inventoryName
    );

    List<Inventory> findByBranchIdAndStatusNotOrderByInventoryNameAsc(
        String branchId,
        InventoryStatus status
    );

    @Modifying
    @Query("""
    UPDATE Inventory i
    SET i.status = :status
    WHERE i.inventoryCategory.id = :categoryId
      AND i.branch.id = :branchId
""")
    void updateStatusByCategoryIdAndBranchId(
        @Param("categoryId") String categoryId,
        @Param("branchId") String branchId,
        @Param("status") InventoryStatus status
    );

    @Modifying
    @Query(value = """
    UPDATE inventories
    SET status = CASE
        WHEN quantity = 0 THEN 'OUT_OF_STOCK'
        WHEN quantity <= minimum_quantity THEN 'LOW'
        ELSE 'GOOD'
    END
    WHERE inventory_category_id = :categoryId
      AND branch_id = :branchId
    """, nativeQuery = true)
    void recalculateStatusByCategoryIdAndBranchId(
        @Param("categoryId") String categoryId,
        @Param("branchId") String branchId
    );
}