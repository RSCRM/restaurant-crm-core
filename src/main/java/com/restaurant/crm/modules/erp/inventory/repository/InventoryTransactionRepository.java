package com.restaurant.crm.modules.erp.inventory.repository;

import com.restaurant.crm.modules.erp.inventory.entity.InventoryTransaction;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;

public interface InventoryTransactionRepository extends
    JpaRepository<InventoryTransaction, String>,
    JpaSpecificationExecutor<InventoryTransaction> {

    Optional<InventoryTransaction> findByIdAndInventoryBranchId(
        String id,
        String branchId
    );

    Page<InventoryTransaction> findByInventoryBranchId(
        String branchId,
        Pageable pageable
    );
}