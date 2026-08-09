package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.order.enums.OrderStatus;
import com.restaurant.crm.modules.erp.order.repository.OrderRepository;
import com.restaurant.crm.modules.erp.table.dto.request.TableSessionCreationRequest;
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
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableSessionServiceImplTest {

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

    TableSessionServiceImpl tableSessionService;

    @BeforeEach
    void setUp() {
        tableSessionService = new TableSessionServiceImpl(
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
    void create_registersGuestAndOccupiesTable() {
        RestaurantTable table = table(RestaurantTableStatus.AVAILABLE, 4);
        TableSessionResponse mapped = TableSessionResponse.builder().id("session-1").build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findByIdForUpdate("table-1")).thenReturn(Optional.of(table));
            when(tableSessionRepository.existsByTableIdAndStatus("table-1", TableSessionStatus.ACTIVE))
                    .thenReturn(false);
            when(tableSessionRepository.save(any(TableSession.class))).thenAnswer(invocation -> {
                TableSession session = invocation.getArgument(0);
                session.setId("session-1");
                return session;
            });
            when(tableSessionMapper.toResponse(any(TableSession.class))).thenReturn(mapped);

            TableSessionResponse response = tableSessionService.create(request(4));

            assertEquals("session-1", response.getId());
            assertEquals(RestaurantTableStatus.OCCUPIED, table.getStatus());
            verify(restaurantTableRepository).save(table);
            verify(orderRepository).save(argThat(order ->
                    "table-1".equals(order.getTableId()) && order.getStatus() == OrderStatus.PENDING));
        }
    }

    @Test
    void create_rejectsUnavailableTable() {
        RestaurantTable table = table(RestaurantTableStatus.OCCUPIED, 4);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findByIdForUpdate("table-1")).thenReturn(Optional.of(table));

            AppException exception = assertThrows(AppException.class, () -> tableSessionService.create(request(2)));

            assertEquals(ErrorCode.TABLE_NOT_AVAILABLE, exception.getErrorCode());
            verify(tableSessionRepository, never()).save(any());
        }
    }

    @Test
    void create_rejectsTableLockedForUpcomingBooking() {
        RestaurantTable table = table(RestaurantTableStatus.AVAILABLE, 4);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findByIdForUpdate("table-1")).thenReturn(Optional.of(table));
            when(bookingRepository.findFirstByTables_IdAndStatusInOrderByBookingTimeAsc(any(), any()))
                    .thenReturn(Optional.of(Booking.builder().bookingTime(Instant.now().plusSeconds(30 * 60)).build()));

            AppException exception = assertThrows(AppException.class, () -> tableSessionService.create(request(2)));

            assertEquals(ErrorCode.TABLE_NOT_AVAILABLE, exception.getErrorCode());
            verify(tableSessionRepository, never()).save(any());
        }
    }

    @Test
    void create_rejectsPartyLargerThanCapacity() {
        RestaurantTable table = table(RestaurantTableStatus.AVAILABLE, 4);

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findByIdForUpdate("table-1")).thenReturn(Optional.of(table));
            when(tableSessionRepository.existsByTableIdAndStatus("table-1", TableSessionStatus.ACTIVE))
                    .thenReturn(false);

            AppException exception = assertThrows(AppException.class, () -> tableSessionService.create(request(5)));

            assertEquals(ErrorCode.TABLE_PARTY_SIZE_EXCEEDS_CAPACITY, exception.getErrorCode());
            verify(restaurantTableRepository, never()).save(any());
        }
    }

    @Test
    void create_hidesTableFromAnotherBranch() {
        RestaurantTable table = table(RestaurantTableStatus.AVAILABLE, 4);
        table.getArea().setBranchId("branch-2");

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findByIdForUpdate("table-1")).thenReturn(Optional.of(table));

            AppException exception = assertThrows(AppException.class, () -> tableSessionService.create(request(2)));

            assertEquals(ErrorCode.TABLE_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void getActiveByTable_returnsSessionInCurrentBranch() {
        TableSession session = TableSession.builder().id("session-1").branchId("branch-1").status(TableSessionStatus.ACTIVE).build();
        TableSessionResponse mapped = TableSessionResponse.builder().id("session-1").build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(activeBranch()));
            when(tableSessionRepository.findByTableIdAndStatus("table-1", TableSessionStatus.ACTIVE))
                    .thenReturn(Optional.of(session));
            when(tableSessionMapper.toResponse(session)).thenReturn(mapped);

            assertEquals("session-1", tableSessionService.getActiveByTable("table-1").getId());
        }
    }

    private OrganizationBranch activeBranch() {
        return OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }

    private RestaurantTable table(RestaurantTableStatus status, int capacity) {
        return RestaurantTable.builder()
                .id("table-1")
                .area(TableArea.builder().id("area-1").branchId("branch-1").build())
                .tableNumber("A01")
                .capacity(capacity)
                .status(status)
                .build();
    }

    private TableSessionCreationRequest request(int partySize) {
        return TableSessionCreationRequest.builder()
                .tableId("table-1")
                .guestName("Guest")
                .guestPhone("0901234567")
                .partySize(partySize)
                .build();
    }
}
