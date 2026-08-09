package com.restaurant.crm.modules.erp.attendance.dto.response;

import com.restaurant.crm.modules.erp.attendance.enums.AttendanceStatus;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;
import java.time.LocalDate;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class AttendanceResponse {
    String id;
    String employeeId;
    String employeeName;
    String username;
    String branchId;
    String shiftAssignmentId;
    LocalDate workDate;
    Instant scheduledStart;
    Instant scheduledEnd;
    Instant checkInAt;
    Instant checkOutAt;
    AttendanceStatus status;
}
