package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.crm.customeraccount.entity.Customer;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.entity.TableSession;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.enums.TableSessionStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableManagementMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.repository.TableAreaRepository;
import com.restaurant.crm.modules.erp.table.repository.TableSessionRepository;
import com.restaurant.crm.modules.erp.table.security.TableBranchGuard;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableReservationManagementServiceImplTest {

    @Mock
    TableAreaRepository tableAreaRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    BookingRepository bookingRepository;
    @Mock
    TableSessionRepository tableSessionRepository;
    @Mock
    TableManagementMapper mapper;
    @Mock
    TableBranchGuard tableBranchGuard;
    @InjectMocks
    TableManagementServiceImpl tableManagementService;

    @Test
    void confirmReservationSeatsGuestAndStartsTableSession() {
        RestaurantTable table = RestaurantTable.builder()
                .id("table-1")
                .area(TableArea.builder().branchId("branch-1").build())
                .status(RestaurantTableStatus.AVAILABLE)
                .build();
        Booking booking = Booking.builder()
                .id("booking-1")
                .customer(Customer.builder().phone("0905000003").build())
                .guestCount(4)
                .status(BookingStatus.PENDING)
                .build();

        when(restaurantTableRepository.findById("table-1")).thenReturn(Optional.of(table));
        when(bookingRepository.findFirstByTables_IdAndStatusInOrderByBookingTimeAsc(
                "table-1",
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        )).thenReturn(Optional.of(booking));

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");

            tableManagementService.confirmReservation("table-1");
        }

        assertEquals(BookingStatus.SEATED, booking.getStatus());
        assertEquals(RestaurantTableStatus.OCCUPIED, table.getStatus());
        verify(tableSessionRepository).save(any(TableSession.class));
    }

    @Test
    void cancelReservationReleasesTableWithoutChangingBookingService() {
        RestaurantTable table = RestaurantTable.builder()
                .id("table-1")
                .area(TableArea.builder().branchId("branch-1").build())
                .status(RestaurantTableStatus.RESERVED)
                .build();
        Booking booking = Booking.builder().id("booking-1").status(BookingStatus.PENDING).build();

        when(restaurantTableRepository.findById("table-1")).thenReturn(Optional.of(table));
        when(bookingRepository.findFirstByTables_IdAndStatusInOrderByBookingTimeAsc(
                "table-1",
                List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
        )).thenReturn(Optional.of(booking));

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");

            tableManagementService.cancelReservation("table-1");
        }

        assertEquals(BookingStatus.CANCELLED, booking.getStatus());
        assertEquals(RestaurantTableStatus.AVAILABLE, table.getStatus());
        verify(bookingRepository).save(booking);
        verify(restaurantTableRepository).save(table);
    }

    @Test
    void confirmReservationRequiresTransferWhenTableIsOccupied() {
        RestaurantTable table = RestaurantTable.builder()
                .id("table-1")
                .area(TableArea.builder().branchId("branch-1").build())
                .status(RestaurantTableStatus.OCCUPIED)
                .build();

        when(restaurantTableRepository.findById("table-1")).thenReturn(Optional.of(table));

        try (MockedStatic<AuthUtils> auth = mockStatic(AuthUtils.class)) {
            auth.when(AuthUtils::getCurrentUserId).thenReturn("user-1");
            auth.when(AuthUtils::getEmployeeId).thenReturn("employee-1");
            auth.when(AuthUtils::getBranchId).thenReturn("branch-1");

            AppException exception = assertThrows(
                    AppException.class,
                    () -> tableManagementService.confirmReservation("table-1"));
            assertEquals(ErrorCode.TABLE_SESSION_ACTIVE_EXISTS, exception.getErrorCode());
        }
    }
}
