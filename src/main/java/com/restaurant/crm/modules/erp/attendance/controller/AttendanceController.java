package com.restaurant.crm.modules.erp.attendance.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.service.interfaces.AttendanceService;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

@RestController
@RequestMapping("/api/v1/erp/attendances")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceController {

    AttendanceService attendanceService;

    @PostMapping("/check-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn() {
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.checkIn())
                .build());
    }

    @PostMapping("/check-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut() {
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.checkOut())
                .build());
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<PagingResponse<AttendanceResponse>>> getMyHistory(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {
        LocalDate resolvedTo = to == null ? LocalDate.now() : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(30) : from;
        return ResponseEntity.ok(ApiResponse.<PagingResponse<AttendanceResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.getMyHistory(resolvedFrom, resolvedTo, page, size))
                .build());
    }
}
