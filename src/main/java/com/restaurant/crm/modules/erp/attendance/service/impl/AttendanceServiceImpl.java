package com.restaurant.crm.modules.erp.attendance.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
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
import com.restaurant.crm.modules.erp.attendance.service.interfaces.AttendanceService;
import com.restaurant.crm.modules.erp.organization.entity.Employee;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.EmployeeStatus;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.EmployeeRepository;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
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

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AttendanceServiceImpl implements AttendanceService {

    AttendanceRepository attendanceRepository;
    ShiftAssignmentRepository shiftAssignmentRepository;
    EmployeeRepository employeeRepository;
    OrganizationBranchRepository organizationBranchRepository;
    AttendanceMapper attendanceMapper;

    @NonFinal
    @Value("${security.jwt.signer-key}")
    String signerKey;

    @Override
    @Transactional(readOnly = true)
    public AttendanceQrResponse getCurrentQr() {
        String organizationId = AuthUtils.getOrganizationId();
        String branchId = AuthUtils.getBranchId();
        if (organizationId == null || branchId == null) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }
        OrganizationBranch branch = organizationBranchRepository.findById(branchId)
                .orElseThrow(() -> new AppException(ErrorCode.ORGANIZATION_BRANCH_NOT_FOUND));
        if (branch.getStatus() != OrganizationBranchStatus.ACTIVE) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_INACTIVE);
        }
        if (branch.getOrganization() == null
                || !branch.getOrganization().getId().equals(organizationId)) {
            throw new AppException(ErrorCode.ATTENDANCE_QR_CONTEXT_MISMATCH);
        }

        Instant now = Instant.now();
        Instant issuedAt = Instant.ofEpochSecond(
                now.getEpochSecond() - now.getEpochSecond() % AttendanceConstants.QR_VALIDITY_SECONDS);
        Instant expiresAt = issuedAt.plusSeconds(AttendanceConstants.QR_VALIDITY_SECONDS);
        String qrSessionId = branchId + ":" + issuedAt.getEpochSecond();

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

        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional
    public AttendanceResponse checkOut() {
        Employee employee = currentEmployee();
        Attendance attendance = attendanceRepository
                .findFirstByShiftAssignmentEmployeeIdAndCheckOutAtIsNullOrderByCheckInAtDesc(employee.getId())
                .orElseThrow(() -> new AppException(ErrorCode.ATTENDANCE_OPEN_RECORD_NOT_FOUND));

        attendance.setCheckOutAt(Instant.now());
        return attendanceMapper.toResponse(attendanceRepository.save(attendance));
    }

    @Override
    @Transactional(readOnly = true)
    public PagingResponse<AttendanceResponse> getMyHistory(
            LocalDate from, LocalDate to, int page, int size) {
        if (from.isAfter(to)) {
            throw new AppException(ErrorCode.ATTENDANCE_DATE_RANGE_INVALID);
        }

        Employee employee = currentEmployee();
        Page<Attendance> result = attendanceRepository
                .findByShiftAssignmentEmployeeIdAndShiftAssignmentWorkDateBetween(
                        employee.getId(), from, to,
                        PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "checkInAt")));

        return PagingResponse.<AttendanceResponse>builder()
                .currentPage(page)
                .pageSize(result.getSize())
                .totalPages(result.getTotalPages())
                .totalElement(result.getTotalElements())
                .data(result.getContent().stream().map(attendanceMapper::toResponse).toList())
                .build();
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
