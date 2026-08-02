package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionDirection;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryTransactionType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;

@Repository
public interface InventoryTransactionRepository extends JpaRepository<InventoryTransaction, String> {
    Page<InventoryTransaction> findByInventoryId(
        String inventoryId,
        Pageable pageable
    );

    Page<InventoryTransaction> findByInventoryIngredientBranchId(
        String branchId,
        Pageable pageable
    );

    Page<InventoryTransaction> findByTransactionType(
        InventoryTransactionType transactionType,
        Pageable pageable
    );

    Page<InventoryTransaction> findByTransactionDirection(
        InventoryTransactionDirection direction,
        Pageable pageable
    );

    Page<InventoryTransaction> findByInventoryIngredientBranchIdAndTransactionType(
        String branchId,
        InventoryTransactionType transactionType,
        Pageable pageable
    );

    Page<InventoryTransaction> findByInventoryIngredientBranchIdAndTransactionTimeBetween(
        String branchId,
        Instant from,
        Instant to,
        Pageable pageable
    );

    Optional<InventoryTransaction> findByIdAndInventoryIngredientBranchId(
        String id,
        String branchId
    );

    Page<InventoryTransaction> findByInventoryIdAndInventoryIngredientBranchId(
        String inventoryId,
        String branchId,
        Pageable pageable
    );
}
