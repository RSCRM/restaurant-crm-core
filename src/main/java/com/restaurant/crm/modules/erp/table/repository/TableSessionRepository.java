package com.restaurant.crm.modules.erp.table.repository;

import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TableSessionRepository extends JpaRepository<TableSession, String> {
    boolean existsByTableIdAndStatus(String tableId, TableSessionStatus status);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select session from TableSession session join fetch session.table table join fetch table.area where session.id = :id")
    Optional<TableSession> findByIdForUpdate(String id);
}
