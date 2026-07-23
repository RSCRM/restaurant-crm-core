package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, String>, JpaSpecificationExecutor<RestaurantTable> {
    boolean existsByIdAndAreaBranchId(String tableId, String branchId);
}
