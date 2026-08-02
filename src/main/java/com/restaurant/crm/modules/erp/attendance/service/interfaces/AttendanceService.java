package com.restaurant.crm.modules.erp.attendance.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.attendance.dto.request.AttendanceCheckInRequest;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceQrResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.EmployeeAttendanceResponse;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

public interface AttendanceService {
    AttendanceQrResponse getCurrentQr(String branchId);

    AttendanceResponse checkIn(AttendanceCheckInRequest request);

    AttendanceResponse checkOut();

    PagingResponse<AttendanceResponse> getMyHistory(LocalDate from, LocalDate to, int page, int size);

    List<EmployeeAttendanceResponse> getBranchAttendance(LocalDate workDate, String branchId);

    PagingResponse<AttendanceResponse> getEmployeeHistory(
            String employeeId, LocalDate from, LocalDate to,
            int page, int size, String branchId);

    SseEmitter subscribe(String branchId);
}
