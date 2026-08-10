package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.response.TableSessionResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableSessionMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.repository.TableSessionRepository;
import com.restaurant.crm.modules.erp.table.repository.TableTransferHistoryRepository;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableSessionCloseServiceImplTest {

    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    TableSessionRepository tableSessionRepository;
    @Mock
    TableTransferHistoryRepository tableTransferHistoryRepository;
    @Mock
    OrderRepository orderRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    TableSessionMapper tableSessionMapper;

    TableSessionServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TableSessionServiceImpl(
                organizationBranchRepository,
                restaurantTableRepository,
                tableSessionRepository,
                tableTransferHistoryRepository,
                orderRepository,
                bookingRepository,
                tableSessionMapper
        );
    }

    @Test
    void close_endsSessionAndMakesTableAvailable() {
        RestaurantTable table = table();
        TableSession session = session(table, TableSessionStatus.ACTIVE, "branch-1");
        TableSessionResponse mapped = TableSessionResponse.builder()
                .id("session-1")
                .status(TableSessionStatus.CLOSED)
                .build();

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1")).thenReturn(Optional.of(session));
            when(orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc("table-1", OrderStatus.PENDING))
                    .thenReturn(Optional.empty());
            when(tableSessionMapper.toResponse(session)).thenReturn(mapped);

            TableSessionResponse response = service.close("session-1");

            assertEquals(TableSessionStatus.CLOSED, response.getStatus());
            assertEquals(TableSessionStatus.CLOSED, session.getStatus());
            assertNotNull(session.getEndedAt());
            assertEquals(RestaurantTableStatus.AVAILABLE, table.getStatus());
            verify(restaurantTableRepository).save(table);
            verify(tableSessionRepository).save(session);
        }
    }

    @Test
    void close_rejectsPendingOrder() {
        RestaurantTable table = table();
        TableSession session = session(table, TableSessionStatus.ACTIVE, "branch-1");

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1")).thenReturn(Optional.of(session));
            when(orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc("table-1", OrderStatus.PENDING))
                    .thenReturn(Optional.of(Order.builder().id("order-1").status(OrderStatus.PENDING).build()));

            AppException exception = assertThrows(AppException.class, () -> service.close("session-1"));

            assertEquals(ErrorCode.TABLE_SESSION_UNPAID_ORDER, exception.getErrorCode());
            verify(restaurantTableRepository, never()).save(any());
        }
    }

    @Test
    void close_rejectsAlreadyClosedSession() {
        TableSession session = session(table(), TableSessionStatus.CLOSED, "branch-1");

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1")).thenReturn(Optional.of(session));

            AppException exception = assertThrows(AppException.class, () -> service.close("session-1"));

            assertEquals(ErrorCode.TABLE_SESSION_NOT_ACTIVE, exception.getErrorCode());
        }
    }

    @Test
    void close_hidesCrossBranchSession() {
        TableSession session = session(table(), TableSessionStatus.ACTIVE, "branch-2");

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(tableSessionRepository.findByIdForUpdate("session-1")).thenReturn(Optional.of(session));

            AppException exception = assertThrows(AppException.class, () -> service.close("session-1"));

            assertEquals(ErrorCode.AUTHZ_UNAUTHORIZED, exception.getErrorCode());
        }
    }

    private OrganizationBranch activeBranch() {
        return OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private RestaurantTable table() {
        return RestaurantTable.builder()
                .id("table-1")
                .area(TableArea.builder().id("area-1").branchId("branch-1").build())
                .tableNumber("A01")
                .capacity(4)
                .status(RestaurantTableStatus.OCCUPIED)
                .build();
    }

    private TableSession session(RestaurantTable table, TableSessionStatus status, String branchId) {
        return TableSession.builder()
                .id("session-1")
                .branchId(branchId)
                .table(table)
                .guestName("Guest")
                .partySize(2)
                .status(status)
                .build();
    }
}
