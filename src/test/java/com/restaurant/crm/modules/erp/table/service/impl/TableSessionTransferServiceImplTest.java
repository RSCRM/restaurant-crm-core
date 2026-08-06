package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.entity.Order;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionTransferRequest;
import com.restaurant.crm.modules.erp.table.dto.response.TableSessionResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.entity.TableTransferHistory;
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
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableSessionTransferServiceImplTest {

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
    void transfer_movesSessionPendingOrderAndTables() {
        RestaurantTable source = table("source", RestaurantTableStatus.OCCUPIED);
        RestaurantTable target = table("target", RestaurantTableStatus.AVAILABLE);
        TableSession session = session(source, TableSessionStatus.ACTIVE);
        Order order = Order.builder().id("order-1").tableId("source").status(OrderStatus.PENDING).build();
        TableSessionResponse mapped = TableSessionResponse.builder().tableId("target").build();

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1")).thenReturn(Optional.of(session));
            when(restaurantTableRepository.findByIdForUpdate("target")).thenReturn(Optional.of(target));
            when(tableSessionRepository.existsByTableIdAndStatus("target", TableSessionStatus.ACTIVE))
                    .thenReturn(false);
            when(orderRepository.findFirstByTableIdAndStatusOrderByCreatedAtDesc("source", OrderStatus.PENDING))
                    .thenReturn(Optional.of(order));
            when(tableSessionMapper.toResponse(session)).thenReturn(mapped);

            TableSessionResponse response = service.transfer("session-1", request("target"));

            assertEquals("target", response.getTableId());
            assertEquals(RestaurantTableStatus.AVAILABLE, source.getStatus());
            assertEquals(RestaurantTableStatus.OCCUPIED, target.getStatus());
            assertEquals(target, session.getTable());
            assertEquals("target", order.getTableId());
            verify(tableTransferHistoryRepository).save(any(TableTransferHistory.class));
        }
    }

    @Test
    void transfer_rejectsUnavailableTarget() {
        RestaurantTable source = table("source", RestaurantTableStatus.OCCUPIED);
        RestaurantTable target = table("target", RestaurantTableStatus.OCCUPIED);

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1"))
                    .thenReturn(Optional.of(session(source, TableSessionStatus.ACTIVE)));
            when(restaurantTableRepository.findByIdForUpdate("target")).thenReturn(Optional.of(target));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> service.transfer("session-1", request("target"))
            );

            assertEquals(ErrorCode.TABLE_NOT_AVAILABLE, exception.getErrorCode());
            verify(tableTransferHistoryRepository, never()).save(any());
        }
    }

    @Test
    void transfer_rejectsInactiveSession() {
        RestaurantTable source = table("source", RestaurantTableStatus.AVAILABLE);

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1"))
                    .thenReturn(Optional.of(session(source, TableSessionStatus.CLOSED)));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> service.transfer("session-1", request("target"))
            );

            assertEquals(ErrorCode.TABLE_SESSION_NOT_ACTIVE, exception.getErrorCode());
        }
    }

    @Test
    void transfer_hidesCrossBranchTarget() {
        RestaurantTable source = table("source", RestaurantTableStatus.OCCUPIED);
        RestaurantTable target = table("target", RestaurantTableStatus.AVAILABLE);
        target.getArea().setBranchId("branch-2");

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByIdForUpdate("session-1"))
                    .thenReturn(Optional.of(session(source, TableSessionStatus.ACTIVE)));
            when(restaurantTableRepository.findByIdForUpdate("target")).thenReturn(Optional.of(target));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> service.transfer("session-1", request("target"))
            );

            assertEquals(ErrorCode.TABLE_NOT_FOUND, exception.getErrorCode());
        }
    }

    private OrganizationBranch activeBranch() {
        return OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private RestaurantTable table(String id, RestaurantTableStatus status) {
        return RestaurantTable.builder()
                .id(id)
                .area(TableArea.builder().id("area-1").branchId("branch-1").build())
                .tableNumber(id)
                .capacity(4)
                .status(status)
                .build();
    }

    private TableSession session(RestaurantTable table, TableSessionStatus status) {
        return TableSession.builder()
                .id("session-1")
                .branchId("branch-1")
                .table(table)
                .guestName("Guest")
                .partySize(2)
                .status(status)
                .build();
    }

    private TableSessionTransferRequest request(String targetTableId) {
        return TableSessionTransferRequest.builder().targetTableId(targetTableId).build();
    }
}
