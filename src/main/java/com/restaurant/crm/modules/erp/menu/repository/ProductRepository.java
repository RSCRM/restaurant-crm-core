package com.restaurant.crm.modules.erp.menu.repository;

import com.restaurant.crm.modules.erp.menu.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByIdAndBranchId(String productId, String branchId);

    List<Product> findByBranchIdOrderByCategoryIdAscProductNameAsc(String branchId);

    List<Product> findByBranchIdAndDeletedAtIsNullOrderByCategoryIdAscProductNameAsc(String branchId);

    Optional<Product> findByIdAndDeletedAtIsNull(String productId);

    boolean existsByBranch_IdAndProductName(String branchId, String productName);

    boolean existsByBranch_IdAndProductNameAndIdNot(String branchId, String productName, String id);

    List<Product> findByCategory_Id(String categoryId);

    @Modifying
    @Query("update Product p set p.category = null where p.category.id = :categoryId")
    void detachCategory(@Param("categoryId") String categoryId);
}
