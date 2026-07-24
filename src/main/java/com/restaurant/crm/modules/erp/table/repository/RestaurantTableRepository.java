package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RestaurantTableRepository extends JpaRepository<RestaurantTable, String> {
    boolean existsByIdAndAreaBranchId(String tableId, String branchId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select table from RestaurantTable table join fetch table.area where table.id = :tableId")
    Optional<RestaurantTable> findByIdForUpdate(String tableId);
}
