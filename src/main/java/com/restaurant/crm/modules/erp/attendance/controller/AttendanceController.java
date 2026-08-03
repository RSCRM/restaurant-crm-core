package com.restaurant.crm.modules.erp.attendance.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.attendance.constants.permission.AttendancePermissionConstants;
import com.restaurant.crm.modules.erp.attendance.dto.request.AttendanceCheckInRequest;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceQrResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.EmployeeAttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.service.interfaces.AttendanceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/attendances")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceController {

    AttendanceService attendanceService;

    @GetMapping("/qr")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.QR_DISPLAY + "')")
    public ResponseEntity<ApiResponse<AttendanceQrResponse>> getCurrentQr(
            @RequestParam(required = false) String branchId) {
        return ResponseEntity.ok(ApiResponse.<AttendanceQrResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.getCurrentQr(branchId))
                .build());
    }

    @PostMapping("/check-in")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.SELF_WRITE + "')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkIn(
            @Valid @RequestBody AttendanceCheckInRequest request) {
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.checkIn(request))
                .build());
    }

    @PostMapping("/check-out")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.SELF_WRITE + "')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOut() {
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.checkOut())
                .build());
    }

    @PostMapping("/check-out/qr")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.SELF_WRITE + "')")
    public ResponseEntity<ApiResponse<AttendanceResponse>> checkOutWithQr(
            @Valid @RequestBody AttendanceCheckInRequest request) {
        return ResponseEntity.ok(ApiResponse.<AttendanceResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.checkOutWithQr(request))
                .build());
    }

    @GetMapping("/me")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.SELF_READ + "')")
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

    @GetMapping("/branch")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.BRANCH_READ + "')")
    public ResponseEntity<ApiResponse<List<EmployeeAttendanceResponse>>> getBranchAttendance(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) String branchId) {
        return ResponseEntity.ok(ApiResponse.<List<EmployeeAttendanceResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.getBranchAttendance(
                        date == null ? LocalDate.now() : date, branchId))
                .build());
    }

    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.BRANCH_READ + "')")
    public SseEmitter subscribe(@RequestParam(required = false) String branchId) {
        return attendanceService.subscribe(branchId);
    }

    @GetMapping("/branch/employees/{employeeId}/history")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.BRANCH_READ + "')")
    public ResponseEntity<ApiResponse<PagingResponse<AttendanceResponse>>> getEmployeeHistory(
            @PathVariable String employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String branchId) {
        LocalDate resolvedTo = to == null ? LocalDate.now() : to;
        LocalDate resolvedFrom = from == null ? resolvedTo.minusDays(30) : from;
        return ResponseEntity.ok(ApiResponse.<PagingResponse<AttendanceResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.getEmployeeHistory(
                        employeeId, resolvedFrom, resolvedTo, page, size, branchId))
                .build());
    }

    @GetMapping("/branch/history")
    @PreAuthorize("hasAuthority('" + AttendancePermissionConstants.BRANCH_READ + "')")
    public ResponseEntity<ApiResponse<PagingResponse<AttendanceResponse>>> getBranchHistory(
            @RequestParam(required = false) String employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "1") @Min(1) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(required = false) String branchId) {
        return ResponseEntity.ok(ApiResponse.<PagingResponse<AttendanceResponse>>builder()
                .success(ApiConstant.SUCCESS)
                .data(attendanceService.getBranchHistory(
                        employeeId, date, page, size, branchId))
                .build());
    }
}
