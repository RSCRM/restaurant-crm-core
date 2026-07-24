package com.restaurant.crm.modules.erp.attendance.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.attendance.constants.AttendanceConstants;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = AttendanceConstants.TABLE_SHIFT_ASSIGNMENT,
        uniqueConstraints = @UniqueConstraint(
                name = "uk_shift_assignments_employee_start",
                columnNames = {AttendanceConstants.COL_EMPLOYEE_ID, AttendanceConstants.COL_START_AT}))
public class ShiftAssignment extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = AttendanceConstants.COL_EMPLOYEE_ID, nullable = false)
    Employee employee;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = AttendanceConstants.COL_BRANCH_ID, nullable = false)
    OrganizationBranch branch;

    @Column(name = AttendanceConstants.COL_WORK_DATE, nullable = false)
    LocalDate workDate;

    @Column(name = AttendanceConstants.COL_START_AT, nullable = false)
    Instant startAt;

    @Column(name = AttendanceConstants.COL_END_AT, nullable = false)
    Instant endAt;
}
