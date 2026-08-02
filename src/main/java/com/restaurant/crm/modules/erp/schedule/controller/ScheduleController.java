package com.restaurant.crm.modules.erp.schedule.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.schedule.constants.SchedulePermissionConstants;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleCreationRequest;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleUpdateRequest;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.service.interfaces.ScheduleService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/erp/schedules")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleController {

    ScheduleService scheduleService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<PersonalScheduleResponse>>> getPersonalSchedule(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from == null ? LocalDate.now() : from;
        LocalDate effectiveTo = to == null ? effectiveFrom : to;
        List<PersonalScheduleResponse> response =
                scheduleService.getPersonalSchedule(effectiveFrom, effectiveTo);

        return ResponseEntity.ok(ApiResponse.<List<PersonalScheduleResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }

    @GetMapping("/staff/{employeeId}")
    @PreAuthorize("hasAuthority('" + SchedulePermissionConstants.SCHEDULE_STAFF_READ + "')")
    public ResponseEntity<ApiResponse<List<PersonalScheduleResponse>>> getStaffSchedule(
            @PathVariable String employeeId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from == null ? LocalDate.now() : from;
        LocalDate effectiveTo = to == null ? effectiveFrom : to;
        return ResponseEntity.ok(ApiResponse.<List<PersonalScheduleResponse>>builder()
                .success(true)
                .data(scheduleService.getStaffSchedule(employeeId, effectiveFrom, effectiveTo))
                .build());
    }

    @GetMapping("/staff")
    @PreAuthorize("hasAuthority('" + SchedulePermissionConstants.SCHEDULE_STAFF_READ + "')")
    public ResponseEntity<ApiResponse<List<PersonalScheduleResponse>>> getManagedSchedules(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to
    ) {
        LocalDate effectiveFrom = from == null ? LocalDate.now() : from;
        LocalDate effectiveTo = to == null ? effectiveFrom : to;
        return ResponseEntity.ok(ApiResponse.<List<PersonalScheduleResponse>>builder()
                .success(true)
                .data(scheduleService.getManagedSchedules(effectiveFrom, effectiveTo))
                .build());
    }

    @PostMapping
    @PreAuthorize("hasAuthority('" + SchedulePermissionConstants.SCHEDULE_MANAGE + "')")
    public ResponseEntity<ApiResponse<PersonalScheduleResponse>> createSchedule(
            @Valid @RequestBody ScheduleCreationRequest request
    ) {
        return ResponseEntity.status(201).body(ApiResponse.<PersonalScheduleResponse>builder()
                .success(true)
                .data(scheduleService.createSchedule(request))
                .build());
    }

    @PutMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('" + SchedulePermissionConstants.SCHEDULE_MANAGE + "')")
    public ResponseEntity<ApiResponse<PersonalScheduleResponse>> updateSchedule(
            @PathVariable String scheduleId,
            @Valid @RequestBody ScheduleUpdateRequest request
    ) {
        return ResponseEntity.ok(ApiResponse.<PersonalScheduleResponse>builder()
                .success(true)
                .data(scheduleService.updateSchedule(scheduleId, request))
                .build());
    }

    @DeleteMapping("/{scheduleId}")
    @PreAuthorize("hasAuthority('" + SchedulePermissionConstants.SCHEDULE_MANAGE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteSchedule(@PathVariable String scheduleId) {
        scheduleService.deleteSchedule(scheduleId);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(true).build());
    }
}

