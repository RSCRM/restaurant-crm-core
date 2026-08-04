package com.restaurant.crm.modules.erp.attendance.repository;

import com.restaurant.crm.modules.erp.attendance.entity.Attendance;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, String> {
    boolean existsByShiftAssignmentId(String shiftAssignmentId);

    Optional<Attendance> findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc(
            String employeeId);

    Page<Attendance> findByShiftAssignmentEmployeeIdAndShiftAssignmentWorkDateBetween(
            String employeeId, LocalDate from, LocalDate to, Pageable pageable);

    List<Attendance> findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(
            String branchId, LocalDate workDate);

    Page<Attendance> findByShiftAssignmentBranchId(String branchId, Pageable pageable);

    Page<Attendance> findByShiftAssignmentBranchIdAndShiftAssignmentEmployeeId(
            String branchId, String employeeId, Pageable pageable);

    Page<Attendance> findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(
            String branchId, LocalDate workDate, Pageable pageable);

    Page<Attendance> findByShiftAssignmentBranchIdAndShiftAssignmentEmployeeIdAndShiftAssignmentWorkDate(
            String branchId, String employeeId, LocalDate workDate, Pageable pageable);
}
