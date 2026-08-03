package com.restaurant.crm.modules.erp.attendance.entity;

import com.restaurant.crm.common.entity.BaseEntity;
import com.restaurant.crm.modules.erp.attendance.constants.AttendanceConstants;
import com.restaurant.crm.modules.erp.attendance.enums.AttendanceStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@SuperBuilder
@FieldDefaults(level = AccessLevel.PRIVATE)
@Entity
@Table(name = AttendanceConstants.TABLE_ATTENDANCE)
public class Attendance extends BaseEntity {

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = AttendanceConstants.COL_SHIFT_ASSIGNMENT_ID, nullable = false, unique = true)
    ShiftAssignment shiftAssignment;

    @Column(name = AttendanceConstants.COL_CHECK_IN_AT, nullable = false)
    Instant checkInAt;

    @Column(name = AttendanceConstants.COL_CHECK_OUT_AT)
    Instant checkOutAt;

    @Enumerated(EnumType.STRING)
    @Column(name = AttendanceConstants.COL_STATUS, nullable = false)
    AttendanceStatus status;
}
