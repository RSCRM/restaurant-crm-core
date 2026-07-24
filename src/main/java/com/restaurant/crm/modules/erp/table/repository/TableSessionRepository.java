package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, String> {
    boolean existsByTableIdAndStatus(String tableId, TableSessionStatus status);
}
