package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.Inventory;
import com.restaurant.crm.modules.erp.inventory.enums.InventoryStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface InventoryRepository extends JpaRepository<Inventory, String> {
    Optional<Inventory> findByIngredientId(
        String ingredientId
    );

    boolean existsByIngredientId(
        String ingredientId
    );

    Page<Inventory> findByStatus(
        InventoryStatus status,
        Pageable pageable
    );

    Page<Inventory> findByIngredientBranchId(
        String branchId,
        Pageable pageable
    );

    Page<Inventory> findByIngredientIngredientNameContainingIgnoreCase(
        String ingredientName,
        Pageable pageable
    );
}