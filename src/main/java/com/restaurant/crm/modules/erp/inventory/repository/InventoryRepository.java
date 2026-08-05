package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

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

    Page<Inventory> findByBranchIdAndInventoryNameContainingIgnoreCase(
        String branchId,
        String inventoryName,
        Pageable pageable
    );

    boolean existsByBranchIdAndInventoryName(
        String branchId,
        String inventoryName
    );

    Optional<Inventory> findByInventoryNameAndBranchId(
        String inventoryName,
        String branchId
    );
}