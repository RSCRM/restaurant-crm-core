package com.restaurant.crm.modules.erp.qr_ordering.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.Organization;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.qr_ordering.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.qr_ordering.dto.response.QrSessionResponse;
import com.restaurant.crm.modules.erp.qr_ordering.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.qr_ordering.model.GroupQrPayload;
import com.restaurant.crm.modules.erp.qr_ordering.model.TableQrPayload;
import com.restaurant.crm.modules.erp.qr_ordering.repository.QrSessionRedisRepository;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.CustomerSessionTokenService;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.GroupQrTokenService;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.OtpTicketVerifier;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.TableQrTokenService;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.time.Instant;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

/**
 * BR-CST-GRP-01 / acceptance: N customers scanning the same free table at the same instant
 * must yield exactly one OWNER + one sessionId; everyone else gets {@code TQR_1011}.
 * The table reservation is modelled by an atomic compare-and-set that stands in for Redis SETNX.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class QrSessionConcurrencyTest {

    private static final String ORG = "organization-1";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final int THREADS = 10;

    @Mock TableQrTokenService tableQrTokenService;
    @Mock GroupQrTokenService groupQrTokenService;
    @Mock CustomerSessionTokenService customerSessionTokenService;
    @Mock OtpTicketVerifier otpTicketVerifier;
    @Mock QrSessionRedisRepository sessionRedisRepository;
    @Mock OrganizationBranchRepository organizationBranchRepository;
    @Mock RestaurantTableRepository restaurantTableRepository;
    @InjectMocks QrSessionServiceImpl service;

    @Test
    void tenConcurrentStartsElectExactlyOneOwner() throws InterruptedException {
        when(tableQrTokenService.verify(anyString()))
                .thenReturn(new TableQrPayload(ORG, BRANCH, TABLE, 1));
        when(organizationBranchRepository.findById(BRANCH)).thenReturn(Optional.of(activeBranch()));
        when(restaurantTableRepository.existsByIdAndAreaBranchId(TABLE, BRANCH)).thenReturn(true);
        when(restaurantTableRepository.findById(TABLE)).thenReturn(Optional.of(table()));
        when(otpTicketVerifier.isValid(anyString(), anyString())).thenReturn(true);
        when(groupQrTokenService.generate(any(GroupQrPayload.class), anyLong())).thenReturn("group-qr");
        when(customerSessionTokenService.issue(anyString(), anyString(), anyString(), anyString(),
                anyString(), any(SessionMemberRole.class), anyLong()))
                .thenReturn(new CustomerSessionTokenService.IssuedToken("token",
                        Instant.now().plusSeconds(3600)));

        // Atomic stand-in for Redis SETNX: only the first caller reserves the table.
        AtomicBoolean reserved = new AtomicBoolean(false);
        when(sessionRedisRepository.tryReserveTable(anyString(), anyString(), anyString(), anyLong()))
                .thenAnswer(invocation -> reserved.compareAndSet(false, true));

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);
        AtomicInteger owners = new AtomicInteger();
        AtomicInteger rejected = new AtomicInteger();

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    QrSessionResponse response = service.start(request());
                    if (response.getRole() == SessionMemberRole.OWNER) {
                        owners.incrementAndGet();
                    }
                } catch (AppException exception) {
                    if (exception.getErrorCode() == ErrorCode.TQR_TABLE_SESSION_EXISTS) {
                        rejected.incrementAndGet();
                    }
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown();
        done.await();
        pool.shutdownNow();

        assertEquals(1, owners.get(), "exactly one OWNER must be elected");
        assertEquals(THREADS - 1, rejected.get(), "all other scanners must get TQR_1011");
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

    private QrSessionStartRequest request() {
        return QrSessionStartRequest.builder()
                .qrToken("table-qr").customerPhone("0900000000").otpTicket("ticket").build();
    }
}
