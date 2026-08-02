package com.restaurant.crm.modules.erp.schedule.service.interfaces;

import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleCreationRequest;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleUpdateRequest;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<PersonalScheduleResponse> getPersonalSchedule(LocalDate from, LocalDate to);

    List<PersonalScheduleResponse> getStaffSchedule(String employeeId, LocalDate from, LocalDate to);

    List<PersonalScheduleResponse> getManagedSchedules(LocalDate from, LocalDate to);

    PersonalScheduleResponse createSchedule(ScheduleCreationRequest request);

    PersonalScheduleResponse updateSchedule(String scheduleId, ScheduleUpdateRequest request);

    void deleteSchedule(String scheduleId);
}

