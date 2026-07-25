package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionJoinRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrSessionResponse;
import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.order.model.GroupQrPayload;
import com.restaurant.crm.modules.erp.order.model.QrSessionData;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import com.restaurant.crm.modules.erp.order.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.order.service.interfaces.CustomerSessionTokenService;
import com.restaurant.crm.modules.erp.order.service.interfaces.GroupQrTokenService;
import com.restaurant.crm.modules.erp.order.service.interfaces.OtpTicketVerifier;
import com.restaurant.crm.modules.erp.order.service.interfaces.TableQrTokenService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QrSessionServiceImplTest {

    private static final String ORG = "organization-1";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String SESSION = "session-1";

    @Mock TableQrTokenService tableQrTokenService;
    @Mock GroupQrTokenService groupQrTokenService;
    @Mock CustomerSessionTokenService customerSessionTokenService;
    @Mock OtpTicketVerifier otpTicketVerifier;
    @Mock QrSessionRedisRepository sessionRedisRepository;
    @Mock OrganizationBranchRepository organizationBranchRepository;
    @Mock RestaurantTableRepository restaurantTableRepository;
    @InjectMocks QrSessionServiceImpl service;

    @BeforeEach
    void stubTokenIssue() {
        // Only the happy paths reach token issuance; keep this lenient for the error-path tests.
        lenient().when(customerSessionTokenService.issue(anyString(), anyString(), anyString(),
                        anyString(), anyString(), any(SessionMemberRole.class), anyLong()))
                .thenReturn(new CustomerSessionTokenService.IssuedToken("session-token",
                        Instant.now().plusSeconds(3600)));
    }

    // ==== start (OWNER) ====

    @Test
    void startOpensSessionAsOwnerWithoutOrder() {
        stubValidTableContext();
        when(otpTicketVerifier.isValid("0900000000", "ticket")).thenReturn(true);
        when(sessionRedisRepository.tryReserveTable(eq(BRANCH), eq(TABLE), anyString(), anyLong()))
                .thenReturn(true);
        when(groupQrTokenService.generate(any(GroupQrPayload.class), anyLong())).thenReturn("group-qr");

        QrSessionResponse response = service.start(startRequest());

        assertEquals(SessionMemberRole.OWNER, response.getRole());
        assertEquals("group-qr", response.getGroupQrToken());
        assertNull(response.getOrderId());
        assertEquals(1, response.getMemberCount());
        verify(sessionRedisRepository).saveSession(any(QrSessionData.class), anyLong());
    }

    @Test
    void startRejectsInvalidOtpTicketWithoutCreatingSession() {
        stubValidTableContext();
        when(otpTicketVerifier.isValid("0900000000", "ticket")).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> service.start(startRequest()));

        assertEquals(ErrorCode.TQR_OTP_TICKET_INVALID, exception.getErrorCode());
        verify(sessionRedisRepository, never())
                .tryReserveTable(anyString(), anyString(), anyString(), anyLong());
        verify(sessionRedisRepository, never()).saveSession(any(), anyLong());
    }

    @Test
    void startRejectsWhenTableAlreadyReserved() {
        stubValidTableContext();
        when(otpTicketVerifier.isValid("0900000000", "ticket")).thenReturn(true);
        when(sessionRedisRepository.tryReserveTable(eq(BRANCH), eq(TABLE), anyString(), anyLong()))
                .thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> service.start(startRequest()));

        assertEquals(ErrorCode.TQR_TABLE_SESSION_EXISTS, exception.getErrorCode());
        verify(sessionRedisRepository, never()).saveSession(any(), anyLong());
    }

    @Test
    void startRejectsWhenBranchOrganizationMismatch() {
        when(tableQrTokenService.verify("table-qr"))
                .thenReturn(new TableQrPayload("other-org", BRANCH, TABLE, 1));
        when(organizationBranchRepository.findById(BRANCH)).thenReturn(Optional.of(activeBranch()));

        AppException exception = assertThrows(AppException.class, () -> service.start(startRequest()));

        assertEquals(ErrorCode.TQR_CONTEXT_MISMATCH, exception.getErrorCode());
    }

    @Test
    void startRejectsWhenBranchInactive() {
        when(tableQrTokenService.verify("table-qr"))
                .thenReturn(new TableQrPayload(ORG, BRANCH, TABLE, 1));
        OrganizationBranch branch = activeBranch();
        branch.setStatus(OrganizationBranchStatus.INACTIVE);
        when(organizationBranchRepository.findById(BRANCH)).thenReturn(Optional.of(branch));

        AppException exception = assertThrows(AppException.class, () -> service.start(startRequest()));

        assertEquals(ErrorCode.ORGANIZATION_BRANCH_INACTIVE, exception.getErrorCode());
    }

    @Test
    void startRejectsWhenTableNotInBranch() {
        when(tableQrTokenService.verify("table-qr"))
                .thenReturn(new TableQrPayload(ORG, BRANCH, TABLE, 1));
        when(organizationBranchRepository.findById(BRANCH)).thenReturn(Optional.of(activeBranch()));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(TABLE, BRANCH)).thenReturn(false);

        AppException exception = assertThrows(AppException.class, () -> service.start(startRequest()));

        assertEquals(ErrorCode.TQR_TABLE_NOT_IN_BRANCH, exception.getErrorCode());
    }

    // ==== join (MEMBER) ====

    @Test
    void joinAddsMemberToSameSession() {
        when(groupQrTokenService.verify("group-qr"))
                .thenReturn(new GroupQrPayload(ORG, BRANCH, TABLE, SESSION));
        when(sessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(openSession()));
        when(sessionRedisRepository.countMembers(SESSION)).thenReturn(1, 2);

        QrSessionResponse response = service.join(joinRequest());

        assertEquals(SessionMemberRole.MEMBER, response.getRole());
        assertEquals(SESSION, response.getSessionId());
        assertNull(response.getGroupQrToken());
    }

    @Test
    void joinRejectsClosedSession() {
        when(groupQrTokenService.verify("group-qr"))
                .thenReturn(new GroupQrPayload(ORG, BRANCH, TABLE, SESSION));
        when(sessionRedisRepository.findSession(SESSION)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class, () -> service.join(joinRequest()));

        assertEquals(ErrorCode.TQR_SESSION_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    void joinRejectsLockedForPaymentSession() {
        when(groupQrTokenService.verify("group-qr"))
                .thenReturn(new GroupQrPayload(ORG, BRANCH, TABLE, SESSION));
        QrSessionData locked = new QrSessionData(SESSION, ORG, BRANCH, TABLE, "owner-device",
                null, "0900000000", null, QrSessionStatus.LOCKED_FOR_PAYMENT, Instant.now());
        when(sessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(locked));

        AppException exception = assertThrows(AppException.class, () -> service.join(joinRequest()));

        assertEquals(ErrorCode.TQR_SESSION_LOCKED_FOR_PAYMENT, exception.getErrorCode());
    }

    @Test
    void joinRejectsSplicedTokenFromAnotherTable() {
        when(groupQrTokenService.verify("group-qr"))
                .thenReturn(new GroupQrPayload(ORG, BRANCH, "other-table", SESSION));
        when(sessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(openSession()));

        AppException exception = assertThrows(AppException.class, () -> service.join(joinRequest()));

        assertEquals(ErrorCode.TQR_CONTEXT_MISMATCH, exception.getErrorCode());
    }

    @Test
    void joinRejectsWhenMemberLimitReached() {
        when(groupQrTokenService.verify("group-qr"))
                .thenReturn(new GroupQrPayload(ORG, BRANCH, TABLE, SESSION));
        when(sessionRedisRepository.findSession(SESSION)).thenReturn(Optional.of(openSession()));
        when(sessionRedisRepository.countMembers(SESSION)).thenReturn(12);

        AppException exception = assertThrows(AppException.class, () -> service.join(joinRequest()));

        assertEquals(ErrorCode.TQR_SESSION_MEMBER_LIMIT, exception.getErrorCode());
    }

    // ==== bindOrder ====

    @Test
    void bindOrderDelegatesToRedisRepository() {
        service.bindOrder(SESSION, "order-1");
        verify(sessionRedisRepository).bindOrder(eq(SESSION), eq("order-1"), anyLong());
    }

    // ==== fixtures ====

    private void stubValidTableContext() {
        when(tableQrTokenService.verify("table-qr"))
                .thenReturn(new TableQrPayload(ORG, BRANCH, TABLE, 1));
        when(organizationBranchRepository.findById(BRANCH)).thenReturn(Optional.of(activeBranch()));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(TABLE, BRANCH)).thenReturn(true);
        when(restaurantTableRepository.findById(TABLE)).thenReturn(Optional.of(table()));
    }

    private OrganizationBranch activeBranch() {
        return OrganizationBranch.builder()
                .id(BRANCH)
                .branchName("Branch One")
                .organization(Organization.builder().id(ORG).build())
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private RestaurantTable table() {
        return RestaurantTable.builder()
                .id(TABLE)
                .tableNumber("A1")
                .capacity(4)
                .status(RestaurantTableStatus.AVAILABLE)
                .area(TableArea.builder().id("area-1").branchId(BRANCH).areaName("Main").build())
                .build();
    }

    private QrSessionData openSession() {
        return new QrSessionData(SESSION, ORG, BRANCH, TABLE, "owner-device",
                null, "0900000000", null, QrSessionStatus.OPEN, Instant.now());
    }

    private QrSessionStartRequest startRequest() {
        return QrSessionStartRequest.builder()
                .qrToken("table-qr").customerPhone("0900000000").otpTicket("ticket").build();
    }

    private QrSessionJoinRequest joinRequest() {
        return QrSessionJoinRequest.builder().groupQrToken("group-qr").build();
    }
}
