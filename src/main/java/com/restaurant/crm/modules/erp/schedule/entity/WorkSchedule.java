package com.restaurant.crm.modules.erp.schedule.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.schedule.constants.WorkScheduleConstants;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(
        name = WorkScheduleConstants.TABLE_WORK_SCHEDULE,
        uniqueConstraints = @UniqueConstraint(columnNames = {
                WorkScheduleConstants.COL_EMPLOYEE_ID,
                WorkScheduleConstants.COL_WORK_DATE,
                WorkScheduleConstants.COL_START_TIME
        })
)
public class WorkSchedule extends BaseEntity {

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = WorkScheduleConstants.COL_EMPLOYEE_ID, nullable = false)
    Employee employee;

    @NotNull
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = WorkScheduleConstants.COL_BRANCH_ID, nullable = false)
    OrganizationBranch branch;

    @NotNull
    @Column(name = WorkScheduleConstants.COL_WORK_DATE, nullable = false)
    LocalDate workDate;

    @NotNull
    @Column(name = WorkScheduleConstants.COL_START_TIME, nullable = false)
    LocalTime startTime;

    @NotNull
    @Column(name = WorkScheduleConstants.COL_END_TIME, nullable = false)
    LocalTime endTime;

    @Size(max = WorkScheduleConstants.MAX_NOTE_LENGTH)
    @Column(name = WorkScheduleConstants.COL_NOTE, length = WorkScheduleConstants.MAX_NOTE_LENGTH)
    String note;
}

