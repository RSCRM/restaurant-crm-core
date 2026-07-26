package com.restaurant.crm.modules.erp.schedule.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.service.interfaces.ScheduleService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
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
}

