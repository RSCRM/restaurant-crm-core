package com.restaurant.crm.modules.erp.schedule.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.experimental.FieldDefaults;

@Getter
@Builder
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleEmployeeResponse {
    String id;
    String name;
    String fullName;
    String email;
    String branchName;
}
