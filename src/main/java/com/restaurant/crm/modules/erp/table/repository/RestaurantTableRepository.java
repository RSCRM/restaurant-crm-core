package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RestaurantTableRepository
        extends JpaRepository<RestaurantTable, String>, JpaSpecificationExecutor<RestaurantTable> {
    boolean existsByIdAndAreaBranchId(String tableId, String branchId);

    List<RestaurantTable> findByAreaBranchIdOrderByAreaAreaNameAscTableNumberAsc(String branchId);

    List<RestaurantTable> findByAreaIdOrderByTableNumberAsc(String areaId);

    boolean existsByArea_IdAndTableNumber(String areaId, String tableNumber);

    boolean existsByArea_IdAndTableNumberAndIdNot(String areaId, String tableNumber, String id);
}
