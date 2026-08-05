package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.InventoryCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InventoryCategoryRepository extends JpaRepository<InventoryCategory, String> {

    Optional<InventoryCategory> findByIdAndBranchId(
        String id,
        String branchId
    );

    Page<InventoryCategory> findByBranchId(
        String branchId,
        Pageable pageable
    );

    Page<InventoryCategory> findByBranchIdAndCategoryNameContainingIgnoreCase(
        String branchId,
        String categoryName,
        Pageable pageable
    );

    boolean existsByBranchIdAndCategoryName(
        String branchId,
        String categoryName
    );
}