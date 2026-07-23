package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.IngredientCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface IngredientCategoryRepository extends JpaRepository<IngredientCategory, String> {

    List<IngredientCategory> findByBranchId(String branchId);

    Page<IngredientCategory> findByBranchId(
            String branchId,
            Pageable pageable
    );

    List<IngredientCategory> findByCategoryNameContainingIgnoreCase(
            String categoryName
    );

    Page<IngredientCategory> findByCategoryNameContainingIgnoreCase(
            String categoryName,
            Pageable pageable
    );

    boolean existsByBranchIdAndCategoryName(
            String branchId,
            String categoryName
    );
}