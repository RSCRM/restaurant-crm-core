package com.restaurant.crm.modules.erp.schedule.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.schedule.dto.response.PersonalScheduleResponse;
import com.restaurant.crm.modules.erp.schedule.entity.WorkSchedule;
import com.restaurant.crm.modules.erp.schedule.mapper.WorkScheduleMapper;
import com.restaurant.crm.modules.erp.schedule.repository.WorkScheduleRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScheduleServiceImplTest {

    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    WorkScheduleRepository workScheduleRepository;
    @Mock
    WorkScheduleMapper workScheduleMapper;

    ScheduleServiceImpl scheduleService;

    @BeforeEach
    void setUp() {
        scheduleService = new ScheduleServiceImpl(
                employeeRepository,
                workScheduleRepository,
                workScheduleMapper
        );
    }

    @Test
    void getPersonalSchedule_returnsAuthenticatedEmployeeSchedule() {
        LocalDate from = LocalDate.of(2026, 7, 20);
        LocalDate to = LocalDate.of(2026, 7, 26);
        Employee employee = Employee.builder().id("employee-1").status(EmployeeStatus.ACTIVE).build();
        List<WorkSchedule> schedules = List.of(WorkSchedule.builder().id("schedule-1").build());
        List<PersonalScheduleResponse> expected =
                List.of(PersonalScheduleResponse.builder().id("schedule-1").build());

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(workScheduleRepository
                    .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc("employee-1", from, to))
                    .thenReturn(schedules);
            when(workScheduleMapper.toResponseList(schedules)).thenReturn(expected);

            assertEquals(expected, scheduleService.getPersonalSchedule(from, to));
            verify(workScheduleRepository)
                    .findByEmployeeIdAndWorkDateBetweenOrderByWorkDateAscStartTimeAsc("employee-1", from, to);
        }
    }

    @Test
    void getPersonalSchedule_rejectsReversedDateRange() {
        AppException exception = assertThrows(
                AppException.class,
                () -> scheduleService.getPersonalSchedule(
                        LocalDate.of(2026, 7, 2),
                        LocalDate.of(2026, 7, 1)
                )
        );

        assertEquals(ErrorCode.SCHEDULE_DATE_RANGE_INVALID, exception.getErrorCode());
    }

    @Test
    void getPersonalSchedule_rejectsRangeLongerThanThirtyOneDays() {
        AppException exception = assertThrows(
                AppException.class,
                () -> scheduleService.getPersonalSchedule(
                        LocalDate.of(2026, 7, 1),
                        LocalDate.of(2026, 8, 1)
                )
        );

        assertEquals(ErrorCode.SCHEDULE_DATE_RANGE_EXCEEDED, exception.getErrorCode());
    }

    @Test
    void getPersonalSchedule_rejectsInactiveEmployee() {
        LocalDate date = LocalDate.of(2026, 7, 24);
        Employee employee = Employee.builder()
                .id("employee-1")
                .status(EmployeeStatus.INACTIVE)
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            authUtils.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> scheduleService.getPersonalSchedule(date, date)
            );
            assertEquals(ErrorCode.EMPLOYEE_NOT_ACTIVE, exception.getErrorCode());
        }
    }

    @Test
    void getStaffSchedule_rejectsEmployeeFromAnotherBranch() {
        LocalDate date = LocalDate.of(2026, 7, 28);
        Employee employee = Employee.builder()
                .id("employee-2")
                .branch(OrganizationBranch.builder().id("branch-2").build())
                .status(EmployeeStatus.ACTIVE)
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(employeeRepository.findById("employee-2")).thenReturn(Optional.of(employee));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> scheduleService.getStaffSchedule("employee-2", date, date)
            );
            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, exception.getErrorCode());
        }
    }
}
