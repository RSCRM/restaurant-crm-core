package com.restaurant.crm.modules.erp.schedule.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class PersonalScheduleResponse {
    String id;
    String employeeId;
    String employeeName;
    String employeeFullName;
    String branchId;
    String branchName;
    LocalDate workDate;
    LocalTime startTime;
    LocalTime endTime;
    String note;
}

