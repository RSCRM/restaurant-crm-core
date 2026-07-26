package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.TableArea;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TableAreaRepository extends JpaRepository<TableArea, String> {
    List<TableArea> findByBranchIdOrderByDisplayOrderAscAreaNameAsc(String branchId);

    Optional<TableArea> findByIdAndBranchId(String id, String branchId);

    boolean existsByBranchIdAndAreaName(String branchId, String areaName);

    boolean existsByBranchIdAndAreaNameAndIdNot(String branchId, String areaName, String id);
}
