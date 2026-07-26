package com.restaurant.crm.modules.erp.schedule.service.interfaces;

import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;

import java.time.LocalDate;
import java.util.List;

public interface ScheduleService {
    List<PersonalScheduleResponse> getPersonalSchedule(LocalDate from, LocalDate to);
}

