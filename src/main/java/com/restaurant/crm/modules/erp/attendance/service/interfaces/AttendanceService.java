package com.restaurant.crm.modules.erp.attendance.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;

import java.time.LocalDate;

public interface AttendanceService {
    AttendanceResponse checkIn();

    AttendanceResponse checkOut();

    PagingResponse<AttendanceResponse> getMyHistory(LocalDate from, LocalDate to, int page, int size);
}
