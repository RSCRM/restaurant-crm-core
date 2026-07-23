package com.restaurant.crm.modules.erp.schedule.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.schedule.constants.WorkScheduleConstants;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
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

    private void validateDateRange(LocalDate from, LocalDate to) {
        if (from == null || to == null || from.isAfter(to)) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_INVALID);
        }
        if (ChronoUnit.DAYS.between(from, to) + 1 > WorkScheduleConstants.MAX_RANGE_DAYS) {
            throw new AppException(ErrorCode.SCHEDULE_DATE_RANGE_EXCEEDED);
        }
    }
}

