package com.restaurant.crm.modules.erp.attendance.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.entity.Attendance;
import com.restaurant.crm.modules.erp.attendance.entity.ShiftAssignment;
import com.restaurant.crm.modules.erp.attendance.enums.AttendanceStatus;
import com.restaurant.crm.modules.erp.attendance.mapper.AttendanceMapper;
import com.restaurant.crm.modules.erp.attendance.repository.AttendanceRepository;
import com.restaurant.crm.modules.erp.attendance.repository.ShiftAssignmentRepository;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    @Mock
    AttendanceRepository attendanceRepository;
    @Mock
    ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    AttendanceMapper attendanceMapper;
    @InjectMocks
    AttendanceServiceImpl attendanceService;

    @Test
    void checkInCreatesAttendanceForCurrentShift() {
        Employee employee = employee();
        ShiftAssignment shift = ShiftAssignment.builder()
                .id("shift-1")
                .employee(employee)
                .startAt(Instant.now().minusSeconds(60))
                .endAt(Instant.now().plusSeconds(3600))
                .build();
        AttendanceResponse expected = AttendanceResponse.builder().id("attendance-1").build();

        try (MockedStatic<AuthUtils> auth = currentEmployeeContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(shiftAssignmentRepository
                    .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                            eq("employee-1"), any(Instant.class), any(Instant.class)))
                    .thenReturn(Optional.of(shift));
            when(attendanceRepository.save(any(Attendance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(expected);

            assertSame(expected, attendanceService.checkIn());

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());
            assertSame(shift, captor.getValue().getShiftAssignment());
            assertEquals(AttendanceStatus.ON_TIME, captor.getValue().getStatus());
            assertNotNull(captor.getValue().getCheckInAt());
        }
    }

    @Test
    void checkInRejectsDuplicateShift() {
        Employee employee = employee();
        ShiftAssignment shift = ShiftAssignment.builder()
                .id("shift-1")
                .employee(employee)
                .startAt(Instant.now().minusSeconds(60))
                .endAt(Instant.now().plusSeconds(3600))
                .build();

        try (MockedStatic<AuthUtils> auth = currentEmployeeContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(shiftAssignmentRepository
                    .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                            eq("employee-1"), any(Instant.class), any(Instant.class)))
                    .thenReturn(Optional.of(shift));
            when(attendanceRepository.existsByShiftAssignmentId("shift-1")).thenReturn(true);

            AppException exception = assertThrows(AppException.class, attendanceService::checkIn);
            assertEquals(ErrorCode.ATTENDANCE_ALREADY_CHECKED_IN, exception.getErrorCode());
        }
    }

    @Test
    void checkInRejectsWhenNoCurrentShift() {
        Employee employee = employee();

        try (MockedStatic<AuthUtils> auth = currentEmployeeContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(shiftAssignmentRepository
                    .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                            eq("employee-1"), any(Instant.class), any(Instant.class)))
                    .thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class, attendanceService::checkIn);
            assertEquals(ErrorCode.ATTENDANCE_SHIFT_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void checkOutClosesOpenAttendance() {
        Employee employee = employee();
        Attendance attendance = Attendance.builder().checkInAt(Instant.now().minusSeconds(3600)).build();
        AttendanceResponse expected = AttendanceResponse.builder().id("attendance-1").build();

        try (MockedStatic<AuthUtils> auth = currentEmployeeContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(attendanceRepository
                    .findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc("employee-1"))
                    .thenReturn(Optional.of(attendance));
            when(attendanceRepository.save(attendance)).thenReturn(attendance);
            when(attendanceMapper.toResponse(attendance)).thenReturn(expected);

            assertSame(expected, attendanceService.checkOut());
            assertNotNull(attendance.getCheckOutAt());
        }
    }

    private Employee employee() {
        return Employee.builder()
                .id("employee-1")
                .user(User.builder().id("user-1").build())
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    private MockedStatic<AuthUtils> currentEmployeeContext() {
        MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
        auth.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
        return auth;
    }
}
