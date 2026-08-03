package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.Ingredient;
import com.restaurant.crm.modules.erp.inventory.entity.IngredientCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface IngredientRepository extends JpaRepository<Ingredient, String> {

    Optional<Ingredient> findByIdAndBranchId(
        String id,
        String branchId
    );

    // Get all ingredients of a branch
    List<Ingredient> findByBranchId(String branchId);

    Page<Ingredient> findByBranchId(
            String branchId,
            Pageable pageable
    );

    // Get ingredients by category
    List<Ingredient> findByIngredientCategoryId(
            String ingredientCategoryId
    );

    Page<Ingredient> findByIngredientCategoryIdAndBranchId(
        String ingredientCategoryId,
        String branchId,
        Pageable pageable
    );

    // Search ingredient name inside a branch
    List<Ingredient> findByBranchIdAndIngredientNameContainingIgnoreCase(
            String branchId,
            String ingredientName
    );

    Page<Ingredient> findByBranchIdAndIngredientNameContainingIgnoreCase(
            String branchId,
            String ingredientName,
            Pageable pageable
    );

    // Check duplicate ingredient name in same branch
    boolean existsByBranchIdAndIngredientName(
            String branchId,
            String ingredientName
    );

    // Optional: exact search
    Optional<Ingredient> findByBranchIdAndIngredientName(
            String branchId,
            String ingredientName
    );
}