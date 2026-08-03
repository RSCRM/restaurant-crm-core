package com.restaurant.crm.modules.erp.attendance.service.impl;

import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.attendance.constants.AttendanceConstants;
import com.restaurant.crm.modules.erp.attendance.dto.request.AttendanceCheckInRequest;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceQrResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.entity.Attendance;
import com.restaurant.crm.modules.erp.attendance.entity.ShiftAssignment;
import com.restaurant.crm.modules.erp.attendance.enums.AttendanceStatus;
import com.restaurant.crm.modules.erp.attendance.mapper.AttendanceMapper;
import com.restaurant.crm.modules.erp.attendance.repository.AttendanceRepository;
import com.restaurant.crm.modules.erp.attendance.repository.ShiftAssignmentRepository;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.entity.User;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AttendanceServiceImplTest {

    private static final String TEST_SIGNER_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";

    @Mock
    AttendanceRepository attendanceRepository;
    @Mock
    ShiftAssignmentRepository shiftAssignmentRepository;
    @Mock
    EmployeeRepository employeeRepository;
    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    UserProfileRepository userProfileRepository;
    @Mock
    AttendanceMapper attendanceMapper;
    @Mock
    SseEmitterService sseEmitterService;
    @InjectMocks
    AttendanceServiceImpl attendanceService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(attendanceService, "signerKey", TEST_SIGNER_KEY);
    }

    @Test
    void getCurrentQrUsesConfiguredValidityForContext() throws ParseException {
        OrganizationBranch branch = branch("branch-1", "organization-1");

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(branch));

            AttendanceQrResponse response = attendanceService.getCurrentQr(null);
            SignedJWT token = SignedJWT.parse(response.getQrToken());

            assertEquals(AttendanceConstants.QR_VALIDITY_SECONDS,
                    response.getExpiresAt().getEpochSecond() - response.getIssuedAt().getEpochSecond());
            assertEquals("organization-1",
                    token.getJWTClaimsSet().getStringClaim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID));
            assertEquals("branch-1",
                    token.getJWTClaimsSet().getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID));
            assertNotNull(token.getJWTClaimsSet().getStringClaim(AttendanceConstants.CLAIM_NONCE));
        }
    }

    @Test
    void checkInCreatesAttendanceForValidQrAndCurrentShift() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");
        ShiftAssignment shift = currentShift(employee, branch);
        AttendanceResponse expected = AttendanceResponse.builder().id("attendance-1").build();

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            String qrToken = validQr(branch);
            stubEmployeeAndShift(employee, shift);
            when(attendanceRepository.save(any(Attendance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceMapper.toResponse(any(Attendance.class))).thenReturn(expected);

            assertSame(expected, attendanceService.checkIn(request(qrToken)));

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());
            assertSame(shift, captor.getValue().getShiftAssignment());
            assertEquals(AttendanceStatus.ON_TIME, captor.getValue().getStatus());
            assertNotNull(captor.getValue().getCheckInAt());
            verify(sseEmitterService).broadcastToBranch(
                    "branch-1", "ATTENDANCE_UPDATED", "employee-1",
                    "ATTENDANCE_BRANCH_READ");
        }
    }

    @Test
    void checkInMarksAttendanceLateAfterFiveMinutes() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");
        ShiftAssignment shift = currentShift(employee, branch);
        shift.setStartAt(Instant.now().minusSeconds(6 * 60));

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            stubEmployeeAndShift(employee, shift);
            when(attendanceRepository.save(any(Attendance.class)))
                    .thenAnswer(invocation -> invocation.getArgument(0));
            when(attendanceMapper.toResponse(any(Attendance.class)))
                    .thenReturn(AttendanceResponse.builder().build());

            attendanceService.checkIn(request(validQr(branch)));

            ArgumentCaptor<Attendance> captor = ArgumentCaptor.forClass(Attendance.class);
            verify(attendanceRepository).save(captor.capture());
            assertEquals(AttendanceStatus.LATE, captor.getValue().getStatus());
        }
    }

    @Test
    void checkInRejectsTamperedQr() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            String qrToken = validQr(branch);
            String[] parts = qrToken.split("\\.");
            parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
            String tampered = String.join(".", parts);
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> attendanceService.checkIn(request(tampered)));
            assertEquals(ErrorCode.ATTENDANCE_QR_INVALID, exception.getErrorCode());
        }
    }

    @Test
    void checkInRejectsQrThatDoesNotMatchShiftBranch() {
        Employee employee = employee();
        OrganizationBranch qrBranch = branch("branch-1", "organization-1");
        ShiftAssignment shift = currentShift(employee, branch("branch-2", "organization-1"));

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            String qrToken = validQr(qrBranch);
            stubEmployeeAndShift(employee, shift);

            AppException exception = assertThrows(
                    AppException.class,
                    () -> attendanceService.checkIn(request(qrToken)));
            assertEquals(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH, exception.getErrorCode());
        }
    }

    @Test
    void checkInRejectsDuplicateShift() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");
        ShiftAssignment shift = currentShift(employee, branch);

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            String qrToken = validQr(branch);
            stubEmployeeAndShift(employee, shift);
            when(attendanceRepository.existsByShiftAssignmentId("shift-1")).thenReturn(true);

            AppException exception = assertThrows(
                    AppException.class,
                    () -> attendanceService.checkIn(request(qrToken)));
            assertEquals(ErrorCode.ATTENDANCE_ALREADY_CHECKED_IN, exception.getErrorCode());
        }
    }

    @Test
    void checkInRejectsWhenNoCurrentShift() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            String qrToken = validQr(branch);
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(shiftAssignmentRepository
                    .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                            eq("employee-1"), any(Instant.class), any(Instant.class)))
                    .thenReturn(Optional.empty());

            AppException exception = assertThrows(
                    AppException.class,
                    () -> attendanceService.checkIn(request(qrToken)));
            assertEquals(ErrorCode.ATTENDANCE_SHIFT_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void checkOutClosesOpenAttendance() {
        Employee employee = employee();
        Attendance attendance = Attendance.builder()
                .shiftAssignment(currentShift(
                        employee, branch("branch-1", "organization-1")))
                .checkInAt(Instant.now().minusSeconds(3600))
                .build();
        AttendanceResponse expected = AttendanceResponse.builder().id("attendance-1").build();

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(attendanceRepository
                    .findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc("employee-1"))
                    .thenReturn(Optional.of(attendance));
            when(attendanceRepository.save(attendance)).thenReturn(attendance);
            when(attendanceMapper.toResponse(attendance)).thenReturn(expected);

            assertSame(expected, attendanceService.checkOut());
            assertNotNull(attendance.getCheckOutAt());
            verify(sseEmitterService).broadcastToBranch(
                    "branch-1", "ATTENDANCE_UPDATED", "employee-1",
                    "ATTENDANCE_BRANCH_READ");
        }
    }

    @Test
    void checkOutWithQrClosesAttendanceInQrBranch() {
        Employee employee = employee();
        OrganizationBranch branch = branch("branch-1", "organization-1");
        Attendance attendance = Attendance.builder()
                .shiftAssignment(currentShift(employee, branch))
                .checkInAt(Instant.now().minusSeconds(3600))
                .build();

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                    .thenReturn(Optional.of(employee));
            when(attendanceRepository
                    .findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc("employee-1"))
                    .thenReturn(Optional.of(attendance));
            when(attendanceRepository.save(attendance)).thenReturn(attendance);
            when(attendanceMapper.toResponse(attendance))
                    .thenReturn(AttendanceResponse.builder().build());

            attendanceService.checkOutWithQr(request(validQr(branch)));

            assertNotNull(attendance.getCheckOutAt());
        }
    }

    @Test
    void getCurrentQrReloadCreatesNewToken() {
        OrganizationBranch branch = branch("branch-1", "organization-1");

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(branch));

            AttendanceQrResponse first = attendanceService.getCurrentQr(null);
            AttendanceQrResponse second = attendanceService.getCurrentQr(null);

            assertNotEquals(first.getQrSessionId(), second.getQrSessionId());
            assertNotEquals(first.getQrToken(), second.getQrToken());
        }
    }

    @Test
    void ownerCanCreateQrForSelectedOrganizationBranch() {
        OrganizationBranch branch = branch("branch-1", "organization-1");

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getOrganizationId).thenReturn("organization-1");
            auth.when(AuthUtils::getBranchId).thenReturn(null);
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(branch));

            AttendanceQrResponse response = attendanceService.getCurrentQr("branch-1");

            assertNotNull(response.getQrToken());
        }
    }

    @Test
    void getBranchAttendanceIncludesCheckedInAndNotCheckedInEmployees() {
        Employee workingEmployee = employee();
        workingEmployee.getUser().setUsername("waiter");
        Employee idleEmployee = Employee.builder()
                .id("employee-2")
                .user(User.builder().id("user-2").username("chef").build())
                .status(EmployeeStatus.ACTIVE)
                .build();
        ShiftAssignment shift = currentShift(
                workingEmployee, branch("branch-1", "organization-1"));
        Attendance attendance = Attendance.builder()
                .shiftAssignment(shift)
                .checkInAt(Instant.now())
                .status(AttendanceStatus.ON_TIME)
                .build();

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(branch("branch-1", "organization-1")));
            when(employeeRepository.findByBranch_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
                    "branch-1", EmployeeStatus.ACTIVE, "MANAGER"))
                    .thenReturn(List.of(workingEmployee, idleEmployee));
            when(attendanceRepository
                    .findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(
                            "branch-1", LocalDate.now()))
                    .thenReturn(List.of(attendance));
            when(userProfileRepository.findByUser_IdIn(any()))
                    .thenReturn(List.of());

            var result = attendanceService.getBranchAttendance(LocalDate.now(), null);

            assertEquals(2, result.size());
            assertTrue(result.get(0).isWorking());
            assertFalse(result.get(1).isWorking());
            assertEquals("chef", result.get(1).getEmployeeName());
        }
    }

    @Test
    void getEmployeeHistoryRejectsEmployeeOutsideSelectedBranch() {
        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(branch("branch-1", "organization-1")));
            when(employeeRepository.findByIdAndBranch_IdAndOrgRole_RoleNameNot(
                    "employee-2", "branch-1", "MANAGER"))
                    .thenReturn(Optional.empty());

            AppException exception = assertThrows(AppException.class,
                    () -> attendanceService.getEmployeeHistory(
                            "employee-2", LocalDate.now().minusDays(30),
                            LocalDate.now(), 1, 10, null));

            assertEquals(ErrorCode.EMPLOYEE_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void getBranchHistorySupportsAllEmployeesAndOneDate() {
        Employee employee = employee();
        employee.getUser().setUsername("waiter");
        Attendance attendance = Attendance.builder()
                .shiftAssignment(currentShift(
                        employee, branch("branch-1", "organization-1")))
                .checkInAt(Instant.now())
                .status(AttendanceStatus.ON_TIME)
                .build();
        AttendanceResponse mapped = new AttendanceResponse();

        try (MockedStatic<AuthUtils> auth = currentContext()) {
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(branch("branch-1", "organization-1")));
            when(attendanceRepository.findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(
                    eq("branch-1"), eq(LocalDate.now()), any()))
                    .thenReturn(new PageImpl<>(
                            List.of(attendance), PageRequest.of(0, 10), 1));
            when(userProfileRepository.findByUser_IdIn(any())).thenReturn(List.of());
            when(attendanceMapper.toResponse(attendance)).thenReturn(mapped);

            var result = attendanceService.getBranchHistory(
                    null, LocalDate.now(), 1, 10, null);

            assertEquals(1, result.getTotalElement());
            assertEquals("waiter", result.getData().get(0).getEmployeeName());
        }
    }

    private String validQr(OrganizationBranch branch) {
        when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(branch));
        return attendanceService.getCurrentQr(null).getQrToken();
    }

    private void stubEmployeeAndShift(Employee employee, ShiftAssignment shift) {
        when(employeeRepository.findByIdAndUserId("employee-1", "user-1"))
                .thenReturn(Optional.of(employee));
        when(shiftAssignmentRepository
                .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                        eq("employee-1"), any(Instant.class), any(Instant.class)))
                .thenReturn(Optional.of(shift));
    }

    private AttendanceCheckInRequest request(String qrToken) {
        return AttendanceCheckInRequest.builder().qrToken(qrToken).build();
    }

    private ShiftAssignment currentShift(Employee employee, OrganizationBranch branch) {
        return ShiftAssignment.builder()
                .id("shift-1")
                .employee(employee)
                .branch(branch)
                .startAt(Instant.now().minusSeconds(60))
                .endAt(Instant.now().plusSeconds(3600))
                .build();
    }

    private OrganizationBranch branch(String branchId, String organizationId) {
        return OrganizationBranch.builder()
                .id(branchId)
                .organization(Organization.builder().id(organizationId).build())
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private Employee employee() {
        return Employee.builder()
                .id("employee-1")
                .user(User.builder().id("user-1").build())
                .status(EmployeeStatus.ACTIVE)
                .build();
    }

    private MockedStatic<AuthUtils> currentContext() {
        MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class);
        auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
        auth.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
        auth.when(AuthUtils::getOrganizationId).thenReturn("organization-1");
        auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
        return auth;
    }
}
