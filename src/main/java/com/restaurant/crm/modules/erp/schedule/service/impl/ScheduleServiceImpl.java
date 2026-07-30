package com.restaurant.crm.modules.erp.schedule.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.schedule.constants.WorkScheduleConstants;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleCreationRequest;
import com.restaurant.crm.modules.erp.schedule.dto.request.ScheduleUpdateRequest;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.entity.WorkSchedule;
import com.restaurant.crm.modules.erp.schedule.mapper.WorkScheduleMapper;
import com.restaurant.crm.modules.erp.schedule.repository.WorkScheduleRepository;
import com.restaurant.crm.modules.erp.schedule.service.interfaces.ScheduleService;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class ScheduleServiceImpl implements ScheduleService {

    EmployeeRepository employeeRepository;
    WorkScheduleRepository workScheduleRepository;
    WorkScheduleMapper workScheduleMapper;

    @Override
    @Transactional(readOnly = true)
    public List<PersonalScheduleResponse> getPersonalSchedule(LocalDate from, LocalDate to) {
        validateDateRange(from, to);

        Employee employee = employeeRepository
                .findByIdAndUserId(AuthUtils.getEmployeeId(), AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }

        return workScheduleMapper.toResponseList(
                workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        employee.getId(),
                        from,
                        to
                )
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PersonalScheduleResponse> getStaffSchedule(String employeeId, LocalDate from, LocalDate to) {
        validateDateRange(from, to);
        Employee employee = getManagedEmployee(employeeId);

        return workScheduleMapper.toResponseList(
                workScheduleRepository.findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc(
                        employee.getId(),
                        from,
                        to
                )
        );
    }

    @Override
    @Transactional
    public PersonalScheduleResponse createSchedule(ScheduleCreationRequest request) {
        Employee employee = getManagedEmployee(request.getEmployeeId());
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (workScheduleRepository.existsByEmployeeIdAndWorkDateAndStartTime(
                employee.getId(), request.getWorkDate(), request.getStartTime())) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        WorkSchedule schedule = WorkSchedule.builder()
                .employee(employee)
                .branch(employee.getBranch())
                .workDate(request.getWorkDate())
                .startTime(request.getStartTime())
                .endTime(request.getEndTime())
                .note(request.getNote())
                .build();
        return workScheduleMapper.toResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public PersonalScheduleResponse updateSchedule(String scheduleId, ScheduleUpdateRequest request) {
        WorkSchedule schedule = getManagedSchedule(scheduleId);
        validateTimeRange(request.getStartTime(), request.getEndTime());
        if (workScheduleRepository.existsByEmployeeIdAndWorkDateAndStartTimeAndIdNot(
                schedule.getEmployee().getId(),
                request.getWorkDate(),
                request.getStartTime(),
                scheduleId
        )) {
            throw new AppException(ErrorCode.SCHEDULE_CONFLICT);
        }

        schedule.setWorkDate(request.getWorkDate());
        schedule.setStartTime(request.getStartTime());
        schedule.setEndTime(request.getEndTime());
        schedule.setNote(request.getNote());
        return workScheduleMapper.toResponse(workScheduleRepository.save(schedule));
    }

    @Override
    @Transactional
    public void deleteSchedule(String scheduleId) {
        workScheduleRepository.delete(getManagedSchedule(scheduleId));
    }

    private Employee getManagedEmployee(String employeeId) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }
        if (employee.getBranch() == null
                || !Objects.equals(employee.getBranch().getId(), AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return employee;
    }

    private WorkSchedule getManagedSchedule(String scheduleId) {
        WorkSchedule schedule = workScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> new AppException(ErrorCode.SCHEDULE_NOT_FOUND));
        if (!Objects.equals(schedule.getBranch().getId(), AuthUtils.getBranchId())) {
            throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
        }
        return schedule;
    }

    private void validateTimeRange(java.time.LocalTime startTime, java.time.LocalTime endTime) {
        if (startTime == null || endTime == null || !startTime.isBefore(endTime)) {
            throw new AppException(ErrorCode.SCHEDULE_TIME_RANGE_INVALID);
        }
    }

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > WorkScheduleConstants.MAX_RANGE_DAYS) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_EXCEEDED);
        }
    }
}

