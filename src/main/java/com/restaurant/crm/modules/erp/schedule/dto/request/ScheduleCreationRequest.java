package com.restaurant.crm.modules.erp.schedule.dto.request;

import com.restaurant.crm.modules.erp.schedule.constants.WorkScheduleConstants;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.LocalDate;
import java.time.LocalTime;

@Getter
@Setter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class ScheduleCreationRequest {

    @NotBlank
    String employeeId;

    @NotNull
    LocalDate workDate;

    @NotNull
    LocalTime startTime;

    @NotNull
    LocalTime endTime;

    @Size(max = WorkScheduleConstants.MAX_NOTE_LENGTH)
    String note;
}
