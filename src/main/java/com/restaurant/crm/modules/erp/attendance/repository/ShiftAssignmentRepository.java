package com.restaurant.crm.modules.erp.attendance.repository;

import com.restaurant.crm.modules.erp.attendance.entity.ShiftAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.Instant;
import java.util.Optional;

public interface ShiftAssignmentRepository extends JpaRepository<ShiftAssignment, String> {
    Optional<ShiftAssignment> findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
            String employeeId, Instant startAt, Instant endAt);
}
