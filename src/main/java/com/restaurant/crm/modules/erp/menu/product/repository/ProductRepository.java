package com.restaurant.crm.modules.erp.menu.product.repository;

import com.restaurant.crm.modules.erp.menu.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, String> {
    Optional<Product> findByIdAndBranchId(String productId, String branchId);
}
