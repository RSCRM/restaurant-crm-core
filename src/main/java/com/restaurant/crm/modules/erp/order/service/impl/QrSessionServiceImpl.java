package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.order.constants.QrSessionConstants;
import com.restaurant.crm.modules.erp.order.dto.request.QrResolveRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionJoinRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrResolveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.QrSessionResponse;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.model.GroupQrPayload;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.model.QrSessionMember;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSessionTokenService;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupQrTokenService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OtpTicketVerifier;
import com.restaurant.crm.modules.erp.order.service.interfaces.QrSessionService;
import com.restaurant.crm.modules.erp.order.service.interfaces.TableQrTokenService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

/**
 * QR ordering session orchestration (uc-c-02). Branch isolation (NFR-07) is enforced
 * by always deriving org/branch/table from verified tokens, never from client input.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QrSessionServiceImpl implements QrSessionService {

    TableQrTokenService tableQrTokenService;
    GroupQrTokenService groupQrTokenService;
    CustomerSessionTokenService customerSessionTokenService;
    OtpTicketVerifier otpTicketVerifier;
    QrSessionRedisRepository sessionRedisRepository;
    OrganizationBranchRepository organizationBranchRepository;
    RestaurantTableRepository restaurantTableRepository;

    @Override
    @Transactional(readOnly = true)
    public QrResolveResponse resolve(QrResolveRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        TableContext context = validateTableContext(payload);

        boolean hasActiveSession = sessionRedisRepository
                .getTableSessionId(payload.branchId(), payload.tableId())
                .isPresent();

        return QrResolveResponse.builder()
                .organizationId(payload.organizationId())
                .branchId(payload.branchId())
                .branchName(context.branch().getBranchName())
                .areaName(context.table().getArea().getAreaName())
                .tableNumber(context.table().getTableNumber())
                .capacity(context.table().getCapacity())
                .tableStatus(context.table().getStatus().name())
                .joinable(true)
                .hasActiveSession(hasActiveSession)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QrSessionResponse start(QrSessionStartRequest request) {
        TableQrPayload payload = tableQrTokenService.verify(request.getQrToken());
        TableContext context = validateTableContext(payload);

        // OTP is proven by uc-c-03's ticket; uc-c-02 only consumes it. Fail before any write.
        // The ticket must be bound to THIS branch+table (from the verified QR), not just the phone.
        if (!otpTicketVerifier.isValid(request.getCustomerPhone(), payload.branchId(),
                payload.tableId(), request.getOtpTicket())) {
            throw new AppException(ErrorCode.TQR_OTP_TICKET_INVALID);
        }

        String sessionId = UUID.randomUUID().toString();
        String deviceId = UUID.randomUUID().toString();
        Instant now = Instant.now();

        // Atomic owner election — only one concurrent scanner can reserve the table.
        boolean reserved = sessionRedisRepository.tryReserveTable(
                payload.branchId(), payload.tableId(), sessionId, QrSessionConstants.SESSION_TTL_SECONDS);
        if (!reserved) {
            throw new AppException(ErrorCode.TQR_TABLE_SESSION_EXISTS);
        }

        try {
            QrSessionData data = new QrSessionData(
                    sessionId,
                    payload.organizationId(),
                    payload.branchId(),
                    payload.tableId(),
                    deviceId,
                    null,
                    request.getCustomerPhone(),
                    null,
                    QrSessionStatus.OPEN,
                    now);
            sessionRedisRepository.saveSession(data, QrSessionConstants.SESSION_TTL_SECONDS);
            sessionRedisRepository.saveMember(
                    sessionId,
                    new QrSessionMember(deviceId, SessionMemberRole.OWNER, null,
                            request.getCustomerPhone(), now, now),
                    QrSessionConstants.SESSION_TTL_SECONDS);
        } catch (RuntimeException exception) {
            // Roll back the reservation so the table is not left permanently locked.
            sessionRedisRepository.releaseTable(payload.branchId(), payload.tableId());
            throw exception;
        }

        String groupQrToken = groupQrTokenService.generate(
                new GroupQrPayload(payload.organizationId(), payload.branchId(),
                        payload.tableId(), sessionId),
                QrSessionConstants.GROUP_QR_TTL_SECONDS);
        CustomerSessionTokenService.IssuedToken sessionToken = issueSessionToken(
                sessionId, deviceId, payload, SessionMemberRole.OWNER);

        log.info("QR session opened: session={} branch={} table={} (owner)",
                sessionId, payload.branchId(), payload.tableId());

        return QrSessionResponse.builder()
                .sessionId(sessionId)
                .deviceId(deviceId)
                .role(SessionMemberRole.OWNER)
                .sessionToken(sessionToken.token())
                .sessionExpiresAt(sessionToken.expiresAt())
                .groupQrToken(groupQrToken)
                .groupQrExpiresAt(now.plusSeconds(QrSessionConstants.GROUP_QR_TTL_SECONDS))
                .memberCount(1)
                .orderId(null)
                .organizationId(payload.organizationId())
                .branchId(payload.branchId())
                .tableId(payload.tableId())
                .tableNumber(context.table().getTableNumber())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QrSessionResponse join(QrSessionJoinRequest request) {
        GroupQrPayload payload = groupQrTokenService.verify(request.getGroupQrToken());

        QrSessionData session = sessionRedisRepository.findSession(payload.sessionId())
                .orElseThrow(() -> new AppException(ErrorCode.TQR_SESSION_NOT_FOUND));
        assertJoinable(session);

        // Anti-splice: the group QR's branch/table must match the session it claims to join.
        if (!payload.organizationId().equals(session.organizationId())
                || !payload.branchId().equals(session.branchId())
                || !payload.tableId().equals(session.tableId())) {
            throw new AppException(ErrorCode.TQR_CONTEXT_MISMATCH);
        }

        if (sessionRedisRepository.countMembers(payload.sessionId()) >= QrSessionConstants.MAX_MEMBERS) {
            throw new AppException(ErrorCode.TQR_SESSION_MEMBER_LIMIT);
        }

        String deviceId = UUID.randomUUID().toString();
        Instant now = Instant.now();
        sessionRedisRepository.saveMember(
                payload.sessionId(),
                new QrSessionMember(deviceId, SessionMemberRole.MEMBER, null, null, now, now),
                QrSessionConstants.SESSION_TTL_SECONDS);

        CustomerSessionTokenService.IssuedToken sessionToken = issueSessionToken(
                payload.sessionId(), deviceId,
                new TableQrPayload(session.organizationId(), session.branchId(),
                        session.tableId(), null),
                SessionMemberRole.MEMBER);

        log.info("QR session joined: session={} branch={} table={} (member)",
                payload.sessionId(), session.branchId(), session.tableId());

        return QrSessionResponse.builder()
                .sessionId(payload.sessionId())
                .deviceId(deviceId)
                .role(SessionMemberRole.MEMBER)
                .sessionToken(sessionToken.token())
                .sessionExpiresAt(sessionToken.expiresAt())
                .groupQrToken(null) // members never receive the group QR
                .memberCount(sessionRedisRepository.countMembers(payload.sessionId()))
                .orderId(session.orderId())
                .organizationId(session.organizationId())
                .branchId(session.branchId())
                .tableId(session.tableId())
                .tableNumber(loadTableNumber(session.tableId()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QrSessionResponse getCurrent() {
        QrSessionData session = currentSession();
        SessionMemberRole role = currentRole();
        return baseSessionResponse(session, role, AuthUtils.getDeviceId()).build();
    }

    @Override
    @Transactional(readOnly = true)
    public QrSessionResponse refreshGroupQr() {
        SessionMemberRole role = currentRole();
        if (role != SessionMemberRole.OWNER) {
            throw new AppException(ErrorCode.TQR_NOT_SESSION_OWNER);
        }
        QrSessionData session = currentSession();
        assertOpen(session);

        String groupQrToken = groupQrTokenService.generate(
                new GroupQrPayload(session.organizationId(), session.branchId(),
                        session.tableId(), session.sessionId()),
                QrSessionConstants.GROUP_QR_TTL_SECONDS);
        sessionRedisRepository.touchTtl(session, QrSessionConstants.SESSION_TTL_SECONDS);

        return baseSessionResponse(session, role, AuthUtils.getDeviceId())
                .groupQrToken(groupQrToken)
                .groupQrExpiresAt(Instant.now().plusSeconds(QrSessionConstants.GROUP_QR_TTL_SECONDS))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public QrSessionResponse heartbeat() {
        QrSessionData session = currentSession();
        String deviceId = AuthUtils.getDeviceId();

        sessionRedisRepository.getMember(session.sessionId(), deviceId).ifPresent(member ->
                sessionRedisRepository.saveMember(
                        session.sessionId(),
                        new QrSessionMember(member.deviceId(), member.role(), member.customerId(),
                                member.customerPhone(), member.joinedAt(), Instant.now()),
                        QrSessionConstants.SESSION_TTL_SECONDS));
        sessionRedisRepository.touchTtl(session, QrSessionConstants.SESSION_TTL_SECONDS);

        return baseSessionResponse(session, currentRole(), deviceId).build();
    }

    @Override
    public void bindOrder(String sessionId, String orderId) {
        sessionRedisRepository.bindOrder(sessionId, orderId, QrSessionConstants.SESSION_TTL_SECONDS);
        log.info("QR session {} bound to order {}", sessionId, orderId);
    }

    // ==== helpers ====

    private CustomerSessionTokenService.IssuedToken issueSessionToken(
            String sessionId, String deviceId, TableQrPayload payload, SessionMemberRole role) {
        return customerSessionTokenService.issue(
                sessionId, deviceId, payload.organizationId(), payload.branchId(),
                payload.tableId(), role, QrSessionConstants.SESSION_TTL_SECONDS);
    }

    private QrSessionResponse.QrSessionResponseBuilder baseSessionResponse(
            QrSessionData session, SessionMemberRole role, String deviceId) {
        return QrSessionResponse.builder()
                .sessionId(session.sessionId())
                .deviceId(deviceId)
                .role(role)
                .memberCount(sessionRedisRepository.countMembers(session.sessionId()))
                .orderId(session.orderId())
                .organizationId(session.organizationId())
                .branchId(session.branchId())
                .tableId(session.tableId())
                .tableNumber(loadTableNumber(session.tableId()));
    }

    private QrSessionData currentSession() {
        String sessionId = AuthUtils.getSessionId();
        if (sessionId == null || sessionId.isBlank()) {
            throw new AppException(ErrorCode.TQR_SESSION_EXPIRED);
        }
        return sessionRedisRepository.findSession(sessionId)
                .orElseThrow(() -> new AppException(ErrorCode.TQR_SESSION_EXPIRED));
    }

    private SessionMemberRole currentRole() {
        String role = AuthUtils.getSessionRole();
        if (role == null || role.isBlank()) {
            throw new AppException(ErrorCode.TQR_CONTEXT_MISMATCH);
        }
        return SessionMemberRole.valueOf(role);
    }

    private void assertJoinable(QrSessionData session) {
        switch (session.status()) {
            case LOCKED_FOR_PAYMENT -> throw new AppException(ErrorCode.TQR_SESSION_LOCKED_FOR_PAYMENT);
            case CLOSED -> throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
            default -> { /* OPEN — proceed */ }
        }
    }

    private void assertOpen(QrSessionData session) {
        if (session.status() == QrSessionStatus.LOCKED_FOR_PAYMENT) {
            throw new AppException(ErrorCode.TQR_SESSION_LOCKED_FOR_PAYMENT);
        }
        if (session.status() == QrSessionStatus.CLOSED) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
    }

    private TableContext validateTableContext(TableQrPayload payload) {
        OrganizationBranch branch = organizationBranchRepository.findById(payload.branchId())
                .orElseThrow(() -> new AppException(ErrorCode.TQR_CONTEXT_MISMATCH));
        if (branch.getOrganization() == null
                || !branch.getOrganization().getId().equals(payload.organizationId())) {
            throw new AppException(ErrorCode.TQR_CONTEXT_MISMATCH);
        }
        if (branch.getStatus() != OrganizationBranchStatus.ACTIVE) {
            throw new AppException(ErrorCode.ORGANIZATION_BRANCH_INACTIVE);
        }
        if (!restaurantTableRepository.existsByIdAndAreaBranchId(payload.tableId(), payload.branchId())) {
            throw new AppException(ErrorCode.TQR_TABLE_NOT_IN_BRANCH);
        }
        RestaurantTable table = restaurantTableRepository.findById(payload.tableId())
                .orElseThrow(() -> new AppException(ErrorCode.TQR_TABLE_NOT_IN_BRANCH));
        return new TableContext(branch, table);
    }

    private String loadTableNumber(String tableId) {
        return restaurantTableRepository.findById(tableId)
                .map(RestaurantTable::getTableNumber)
                .orElse(null);
    }

    private record TableContext(OrganizationBranch branch, RestaurantTable table) {
    }
}
