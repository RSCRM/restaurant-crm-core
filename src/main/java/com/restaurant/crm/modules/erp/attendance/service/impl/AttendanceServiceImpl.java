package com.restaurant.crm.modules.erp.attendance.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.sse.service.interfaces.SseEmitterService;
import com.restaurant.crm.modules.erp.attendance.constants.AttendanceConstants;
import com.restaurant.crm.modules.erp.attendance.constants.permission.AttendancePermissionConstants;
import com.restaurant.crm.modules.erp.attendance.dto.request.AttendanceCheckInRequest;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceQrResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.AttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.dto.response.EmployeeAttendanceResponse;
import com.restaurant.crm.modules.erp.attendance.entity.Attendance;
import com.restaurant.crm.modules.erp.attendance.entity.ShiftAssignment;
import com.restaurant.crm.modules.erp.attendance.enums.AttendanceStatus;
import com.restaurant.crm.modules.erp.attendance.mapper.AttendanceMapper;
import com.restaurant.crm.modules.erp.attendance.repository.AttendanceRepository;
import com.restaurant.crm.modules.erp.attendance.repository.ShiftAssignmentRepository;
import com.restaurant.crm.modules.erp.attendance.service.interfaces.AttendanceService;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import com.restaurant.crm.modules.profile.entity.UserProfile;
import com.restaurant.crm.modules.profile.repository.UserProfileRepository;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.Base64;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceServiceImpl implements AttendanceService {

    private static final String MANAGER_ROLE = "MANAGER";

    AttendanceRepository attendanceRepository;
    ShiftAssignmentRepository shiftAssignmentRepository;
    EmployeeRepository employeeRepository;
    OrganizationBranchRepository organizationBranchRepository;
    UserProfileRepository userProfileRepository;
    AttendanceMapper attendanceMapper;
    SseEmitterService sseEmitterService;

    @NonFinal
    @Value("${security.jwt.signer-key}")
    String signerKey;

    @Override
    @Transactional(readOnly = true)
    public AttendanceQrResponse getCurrentQr(String requestedBranchId) {
        OrganizationBranch branch = resolveBranch(requestedBranchId);
        String organizationId = branch.getOrganization().getId();
        String branchId = branch.getId();
        if (branch.getStatus() != OrganizationBranchStatus.ACTIVE) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_INACTIVE);
        }
        if (branch.getOrganization() == null
                || !branch.getOrganization().getId().equals(organizationId)) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        Instant now = Instant.now();
        Instant issuedAt = now.truncatedTo(ChronoUnit.SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(AttendanceConstants.QR_VALIDITY_SECONDS);
        String qrSessionId = branchId + ":" + UUID.randomUUID();

        try {
            byte[] dailySecret = deriveDailySecret(organizationId, branchId, issuedAt);
            String nonce = Base64.getUrlEncoder().withoutPadding()
                    .encodeToString(hmac(dailySecret, qrSessionId));
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issueTime(Date.from(issuedAt))
                    .expirationTime(Date.from(expiresAt))
                    .claim(JwtClaimSetConstant.CLAIM_TYPE, AttendanceConstants.QR_TOKEN_TYPE)
                    .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, organizationId)
                    .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, branchId)
                    .claim(AttendanceConstants.CLAIM_QR_SESSION_ID, qrSessionId)
                    .claim(AttendanceConstants.CLAIM_NONCE, nonce)
                    .build();
            SignedJWT qrToken = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            qrToken.sign(new MACSigner(dailySecret));

            return AttendanceQrResponse.builder()
                    .qrToken(qrToken.serialize())
                    .qrSessionId(qrSessionId)
                    .issuedAt(issuedAt)
                    .expiresAt(expiresAt)
                    .build();
        } catch (GeneralSecurityException | JOSEException exception) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_GENERATION_FAILED);
        }
    }

    @Override
    @Transactional
    public AttendanceResponse checkIn(AttendanceCheckInRequest request) {
        Employee employee = currentEmployee();
        Instant now = Instant.now();
        QrContext qrContext = validateQr(request.getQrToken(), now);
        ShiftAssignment shift = shiftAssignmentRepository
                .findFirstByEmployeeIdAndStartAtLessThanEqualAndEndAtGreaterThanEqualOrderByStartAtDesc(
                        employee.getId(), now, now)
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_SHIFT_NOT_FOUND));

        if (shift.getBranch() == null
                || !shift.getBranch().getId().equals(qrContext.branchId())
                || shift.getBranch().getOrganization() == null
                || !shift.getBranch().getOrganization().getId().equals(qrContext.organizationId())) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        if (attendanceRepository.existsByShiftAssignmentId(shift.getId())) {
            throw new AppException(ErrorCode.ATTENDANCE_ALREADY_CHECKED_IN);
        }

        Attendance attendance = Attendance.builder()
                .shiftAssignment(shift)
                .checkInAt(now)
                .status(now.isAfter(shift.getStartAt().plus(
                        AttendanceConstants.LATE_THRESHOLD_MINUTES, ChronoUnit.MINUTES))
                        ? AttendanceStatus.LATE
                        : AttendanceStatus.ON_TIME)
                .build();

        Attendance saved = attendanceRepository.save(attendance);
        broadcastAfterCommit(shift.getBranch().getId(), employee.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut() {
        return completeCheckOut(null);
    }

    @Override
    @Transactional
    public AttendanceResponse checkOutWithQr(AttendanceCheckInRequest request) {
        return completeCheckOut(validateQr(request.getQrToken(), Instant.now()));
    }

    private AttendanceResponse completeCheckOut(QrContext qrContext) {
        Employee employee = currentEmployee();
        Attendance attendance = attendanceRepository
                .findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc(employee.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_OPEN_RECORD_NOT_FOUND));

        if (qrContext != null
                && (!attendance.getShiftAssignment().getBranch().getId().equals(qrContext.branchId())
                || !attendance.getShiftAssignment().getBranch().getOrganization().getId()
                        .equals(qrContext.organizationId()))) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        attendance.setCheckOutAt(Instant.now());
        Attendance saved = attendanceRepository.save(attendance);
        broadcastAfterCommit(
                attendance.getShiftAssignment().getBranch().getId(), employee.getId());
        return attendanceMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AttendanceResponse> getMyHistory(
            LocalDate from, LocalDate to, int page, int size) {
        return getHistory(currentEmployee().getId(), from, to, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AttendanceResponse> getEmployeeHistory(
            String employeeId, LocalDate from, LocalDate to,
            int page, int size, String requestedBranchId) {
        String branchId = resolveBranch(requestedBranchId).getId();
        employeeRepository.findByIdAndBranch_IdAndOrgRole_RoleNameNot(
                        employeeId, branchId, MANAGER_ROLE)
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        return getHistory(employeeId, from, to, page, size);
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AttendanceResponse> getBranchHistory(
            String employeeId, LocalDate workDate,
            int page, int size, String requestedBranchId) {
        String branchId = resolveBranch(requestedBranchId).getId();
        PageRequest pageable = PageRequest.of(
                page - 1, size, Sort.by(Sort.Direction.DESC, "checkInAt"));
        Page<Attendance> result;
        if (employeeId == null) {
            if (workDate == null) {
                LocalDate to = LocalDate.now();
                LocalDate from = to.minusDays(30);
                result = attendanceRepository.findByShiftAssignmentBranchIdAndShiftAssignmentWorkDateBetween(
                        branchId, from, to, pageable);
            } else {
                result = attendanceRepository.findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(
                        branchId, workDate, pageable);
            }
        } else {
            if (workDate == null) {
                LocalDate to = LocalDate.now();
                LocalDate from = to.minusDays(30);
                result = attendanceRepository.findByShiftAssignmentBranchIdAndShiftAssignmentEmployeeIdAndShiftAssignmentWorkDateBetween(
                        branchId, employeeId, from, to, pageable);
            } else {
                result = attendanceRepository
                        .findByShiftAssignmentBranchIdAndShiftAssignmentEmployeeIdAndShiftAssignmentWorkDate(
                                branchId, employeeId, workDate, pageable);
            }
        }
        Map<String, String> namesByUser = userProfileRepository
                .findByUser_IdIn(result.getContent().stream()
                        .map(attendance -> attendance.getShiftAssignment()
                                .getEmployee().getUser().getId())
                        .distinct()
                        .toList())
                .stream()
                .filter(profile -> profile.getFullName() != null
                        && !profile.getFullName().isBlank())
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        UserProfile::getFullName));
        List<AttendanceResponse> data = result.getContent().stream()
                .map(attendance -> {
                    AttendanceResponse response = attendanceMapper.toResponse(attendance);
                    Employee employee = attendance.getShiftAssignment().getEmployee();
                    response.setEmployeeName(namesByUser.getOrDefault(
                            employee.getUser().getId(), employee.getUser().getUsername()));
                    response.setUsername(employee.getUser().getUsername());
                    return response;
                })
                .toList();

        return PagingResponse.<AttendanceResponse>builder()
                .currentPage(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .data(data)
                .build();
    }

    private PagingResponse<AttendanceResponse> getHistory(
            String employeeId, LocalDate from, LocalDate to, int page, int size) {
        if (from.isAfter(to)) {
            throw new AppException(ErrorCode.ATTENDANCE_DATE_RANGE_INVALID);
        }

        Page<Attendance> result = attendanceRepository
                .findByShiftAssignmentEmployeeIdAndShiftAssignmentWorkDateBetween(
                        employeeId, from, to,
                        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "checkInAt")));

        List<AttendanceResponse> data = result.getContent().stream()
                .map(attendance -> {
                    AttendanceResponse response = attendanceMapper.toResponse(attendance);
                    Employee employee = attendance.getShiftAssignment().getEmployee();
                    response.setUsername(employee.getUser().getUsername());
                    response.setEmployeeName(userProfileRepository.findByUser_Id(employee.getUser().getId())
                            .map(UserProfile::getFullName)
                            .orElse(employee.getUser().getUsername()));
                    return response;
                })
                .toList();

        return PagingResponse.<AttendanceResponse>builder()
                .currentPage(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .data(data)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeAttendanceResponse> getBranchAttendance(
            LocalDate workDate, String requestedBranchId) {
        String branchId = resolveBranch(requestedBranchId).getId();

        List<Employee> employees = employeeRepository
                .findByBranch_IdAndStatusAndOrgRole_RoleNameNotOrderByUser_UsernameAsc(
                        branchId, EmployeeStatus.ACTIVE, MANAGER_ROLE);
        Map<String, Attendance> attendanceByEmployee = attendanceRepository
                .findByShiftAssignmentBranchIdAndShiftAssignmentWorkDate(branchId, workDate)
                .stream()
                .collect(Collectors.toMap(
                        attendance -> attendance.getShiftAssignment().getEmployee().getId(),
                        Function.identity(),
                        (first, second) -> second.getCheckInAt().isAfter(first.getCheckInAt())
                                ? second : first));
        Map<String, String> namesByUser = userProfileRepository
                .findByUser_IdIn(employees.stream()
                        .map(employee -> employee.getUser().getId())
                        .toList())
                .stream()
                .filter(profile -> profile.getFullName() != null
                        && !profile.getFullName().isBlank())
                .collect(Collectors.toMap(
                        profile -> profile.getUser().getId(),
                        UserProfile::getFullName));

        return employees.stream().map(employee -> {
            Attendance attendance = attendanceByEmployee.get(employee.getId());
            ShiftAssignment shift = attendance == null ? null : attendance.getShiftAssignment();
            return EmployeeAttendanceResponse.builder()
                    .employeeId(employee.getId())
                    .employeeName(namesByUser.getOrDefault(
                            employee.getUser().getId(), employee.getUser().getUsername()))
                    .username(employee.getUser().getUsername())
                    .email(employee.getUser().getEmail())
                    .workDate(workDate)
                    .scheduledStart(shift == null ? null : shift.getStartAt())
                    .scheduledEnd(shift == null ? null : shift.getEndAt())
                    .checkInAt(attendance == null ? null : attendance.getCheckInAt())
                    .checkOutAt(attendance == null ? null : attendance.getCheckOutAt())
                    .status(attendance == null ? null : attendance.getStatus())
                    .working(LocalDate.now().equals(workDate)
                            && attendance != null
                            && attendance.getCheckOutAt() == null)
                    .build();
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public SseEmitter subscribe(String requestedBranchId) {
        return sseEmitterService.createEmitter(resolveBranch(requestedBranchId).getId());
    }

    private void broadcastAfterCommit(String branchId, String employeeId) {
        Runnable broadcast = () -> sseEmitterService.broadcastToBranch(
                branchId, "ATTENDANCE_UPDATED", employeeId,
                AttendancePermissionConstants.BRANCH_READ);
        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            broadcast.run();
            return;
        }
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        broadcast.run();
                    }
                });
    }

    private OrganizationBranch resolveBranch(String requestedBranchId) {
        String organizationId = AuthUtils.getOrganizationId();
        String contextBranchId = AuthUtils.getBranchId();
        String branchId = contextBranchId == null ? requestedBranchId : contextBranchId;
        if (organizationId == null || branchId == null
                || (contextBranchId != null
                && requestedBranchId != null
                && !contextBranchId.equals(requestedBranchId))) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        OrganizationBranch branch = organizationBranchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        if (branch.getOrganization() == null
                || !organizationId.equals(branch.getOrganization().getId())) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }
        return branch;
    }

    private Employee currentEmployee() {
        Employee employee = employeeRepository
                .findByIdAndUserId(AuthUtils.getEmployeeId(), AuthUtils.getCurrentUserId())
                .orElseThrow(() -> new AppException(ErrorCode.EMPLOYEE_NOT_FOUND));
        if (employee.getStatus() != EmployeeStatus.ACTIVE) {
            throw new AppException(ErrorCode.EMPLOYEE_NOT_ACTIVE);
        }
        return employee;
    }

    private QrContext validateQr(String token, Instant now) {
        String organizationId = AuthUtils.getOrganizationId();
        String branchId = AuthUtils.getBranchId();
        if (organizationId == null || branchId == null) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        try {
            SignedJWT qrToken = SignedJWT.parse(token);
            JWTClaimsSet claims = qrToken.getJWTClaimsSet();
            Instant issuedAt = claims.getIssueTime() == null ? null : claims.getIssueTime().toInstant();
            Instant expiresAt = claims.getExpirationTime() == null ? null : claims.getExpirationTime().toInstant();
            if (issuedAt == null || expiresAt == null
                    || !JWSAlgorithm.HS512.equals(qrToken.getHeader().getAlgorithm())) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_INVALID);
            }

            byte[] dailySecret = deriveDailySecret(organizationId, branchId, issuedAt);
            if (!qrToken.verify(new MACVerifier(dailySecret))) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_INVALID);
            }
            if (!expiresAt.isAfter(now)) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_EXPIRED);
            }
            if (issuedAt.isAfter(now)
                    || ChronoUnit.SECONDS.between(issuedAt, expiresAt)
                    > AttendanceConstants.QR_VALIDITY_SECONDS) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_INVALID);
            }

            String tokenType = claims.getStringClaim(JwtClaimSetConstant.CLAIM_TYPE);
            String tokenOrganizationId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID);
            String tokenBranchId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID);
            String qrSessionId = claims.getStringClaim(AttendanceConstants.CLAIM_QR_SESSION_ID);
            String nonce = claims.getStringClaim(AttendanceConstants.CLAIM_NONCE);
            if (!AttendanceConstants.QR_TOKEN_TYPE.equals(tokenType)
                    || qrSessionId == null || qrSessionId.isBlank()
                    || nonce == null || nonce.isBlank()) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_INVALID);
            }
            if (!organizationId.equals(tokenOrganizationId) || !branchId.equals(tokenBranchId)) {
                throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
            }
            return new QrContext(tokenOrganizationId, tokenBranchId);
        } catch (AppException exception) {
            throw exception;
        } catch (ParseException | JOSEException | GeneralSecurityException exception) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_INVALID);
        }
    }

    private byte[] deriveDailySecret(String organizationId, String branchId, Instant issuedAt)
            throws GeneralSecurityException {
        String dailyScope = issuedAt.atZone(ZoneOffset.UTC).toLocalDate()
                + ":" + organizationId + ":" + branchId;
        return hmac(signerKey.getBytes(StandardCharsets.UTF_8), dailyScope);
    }

    private byte[] hmac(byte[] key, String value) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key, "HmacSHA512"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private record QrContext(String organizationId, String branchId) {
    }
}
