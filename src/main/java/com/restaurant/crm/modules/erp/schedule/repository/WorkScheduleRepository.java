package com.restaurant.crm.modules.erp.schedule.repository;

import com.restaurant.crm.modules.erp.schedule.entity.WorkSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@Repository
public interface WorkScheduleRepository extends JpaRepository<WorkSchedule, String> {

    List<WorkSchedule> findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
            String employeeId,
            LocalDate from,
            LocalDate to
    );

    boolean existsByEmployeeIdAndWorkDateAndStartTime(
            String employeeId,
            LocalDate workDate,
            LocalTime startTime
    );

    boolean existsByEmployeeIdAndWorkDateAndStartTimeAndIdNot(
            String employeeId,
            LocalDate workDate,
            LocalTime startTime,
            String id
    );
}

