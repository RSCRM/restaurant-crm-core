package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, String> {
    List<Category> findByBranch_IdOrderByDisplayOrderAscCategoryNameAsc(String branchId);

    boolean existsByBranch_IdAndCategoryName(String branchId, String categoryName);

    boolean existsByBranch_IdAndCategoryNameAndIdNot(String branchId, String categoryName, String id);
}
