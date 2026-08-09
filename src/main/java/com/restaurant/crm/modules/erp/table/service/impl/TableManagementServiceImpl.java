package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.booking.entity.Booking;
import com.restaurant.crm.modules.erp.booking.enums.BookingStatus;
import com.restaurant.crm.modules.erp.booking.repository.BookingRepository;
import com.restaurant.crm.modules.erp.table.constants.TableManagementConstants;
import com.restaurant.crm.modules.erp.table.constants.TableSessionConstants;
import com.restaurant.crm.modules.erp.table.dto.request.CreateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.CreateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.response.RestaurantTableResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaResponse;
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
import com.restaurant.crm.modules.erp.table.service.interfaces.TableManagementService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableManagementServiceImpl implements TableManagementService {

    TableAreaRepository tableAreaRepository;
    RestaurantTableRepository restaurantTableRepository;
    BookingRepository bookingRepository;
    TableSessionRepository tableSessionRepository;
    TableManagementMapper mapper;
    TableBranchGuard tableBranchGuard;

    // ================= AREA =================

    @Override
    @Transactional
    public TableAreaResponse createArea(CreateAreaRequest request) {
        tableBranchGuard.validateBranchAccess(request.getBranchId());
        if (tableAreaRepository.existsByBranchIdAndAreaName(request.getBranchId(), request.getAreaName())) {
            throw new AppException(ErrorCode.TABLE_AREA_NAME_EXISTS);
        }
        TableArea area = TableArea.builder()
                .branchId(request.getBranchId())
                .areaName(request.getAreaName())
                .description(request.getDescription())
                .displayOrder(request.getDisplayOrder())
                .build();
        area = tableAreaRepository.save(area);
        return mapper.toAreaResponse(area);
    }

    @Override
    @Transactional
    public TableAreaResponse updateArea(String areaId, UpdateAreaRequest request) {
        TableArea area = tableAreaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(area.getBranchId());
        if (!area.getAreaName().equals(request.getAreaName())
                && tableAreaRepository.existsByBranchIdAndAreaNameAndIdNot(area.getBranchId(), request.getAreaName(), areaId)) {
            throw new AppException(ErrorCode.TABLE_AREA_NAME_EXISTS);
        }
        area.setAreaName(request.getAreaName());
        area.setDescription(request.getDescription());
        area.setDisplayOrder(request.getDisplayOrder());
        area = tableAreaRepository.save(area);
        return mapper.toAreaResponse(area);
    }

    @Override
    @Transactional
    public void deleteArea(String areaId) {
        TableArea area = tableAreaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(area.getBranchId());
        List<RestaurantTable> tables = restaurantTableRepository.findByAreaIdOrderByTableNumberAsc(areaId);
        restaurantTableRepository.deleteAll(tables);
        tableAreaRepository.delete(area);
    }

    @Override
    @Transactional(readOnly = true)
    public List<TableAreaResponse> listAreasByBranch(String branchId) {
        tableBranchGuard.validateBranchAccess(branchId);
        return tableAreaRepository.findByBranchIdOrderByDisplayOrderAscAreaNameAsc(branchId)
                .stream().map(mapper::toAreaResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public TableAreaResponse getArea(String areaId) {
        TableArea area = tableAreaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(area.getBranchId());
        return mapper.toAreaResponse(area);
    }

    // ================= TABLE =================

    @Override
    @Transactional
    public RestaurantTableResponse createTable(CreateTableRequest request) {
        TableArea area = tableAreaRepository.findById(request.getAreaId())
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(area.getBranchId());
        if (restaurantTableRepository.existsByArea_IdAndTableNumber(area.getId(), request.getTableNumber())) {
            throw new AppException(ErrorCode.RESTAURANT_TABLE_NUMBER_EXISTS);
        }
        RestaurantTable table = RestaurantTable.builder()
                .area(area)
                .tableNumber(request.getTableNumber())
                .capacity(request.getCapacity())
                .status(request.getStatus() != null ? request.getStatus() : TableManagementConstants.DEFAULT_STATUS)
                .positionX(request.getPositionX())
                .positionY(request.getPositionY())
                .build();
        table = restaurantTableRepository.save(table);
        return mapper.toTableResponse(table);
    }

    @Override
    @Transactional
    public RestaurantTableResponse updateTable(String tableId, UpdateTableRequest request) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_TABLE_NOT_FOUND));
        TableArea currentArea = table.getArea();
        tableBranchGuard.validateBranchAccess(currentArea.getBranchId());

        TableArea targetArea = currentArea;
        if (request.getAreaId() != null && !request.getAreaId().isBlank()
                && !request.getAreaId().equals(currentArea.getId())) {
            targetArea = tableAreaRepository.findById(request.getAreaId())
                    .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
            if (!targetArea.getBranchId().equals(currentArea.getBranchId())) {
                throw new AppException(ErrorCode.AUTHZ_UNAUTHORIZED);
            }
        }

        if (restaurantTableRepository.existsByArea_IdAndTableNumberAndIdNot(
                targetArea.getId(), request.getTableNumber(), tableId)) {
            throw new AppException(ErrorCode.RESTAURANT_TABLE_NUMBER_EXISTS);
        }

        table.setArea(targetArea);
        table.setTableNumber(request.getTableNumber());
        table.setCapacity(request.getCapacity());
        if (request.getStatus() != null) {
            table.setStatus(request.getStatus());
        }
        table.setPositionX(request.getPositionX());
        table.setPositionY(request.getPositionY());
        table = restaurantTableRepository.save(table);
        return mapper.toTableResponse(table);
    }

    @Override
    @Transactional
    public void deleteTable(String tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_TABLE_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(table.getArea().getBranchId());
        if (table.getStatus() != RestaurantTableStatus.AVAILABLE) {
            throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
        }
        table.setStatus(RestaurantTableStatus.DELETED);
        restaurantTableRepository.save(table);
    }

    @Override
    @Transactional(readOnly = true)
    public List<RestaurantTableResponse> listTablesByArea(String areaId) {
        TableArea area = tableAreaRepository.findById(areaId)
                .orElseThrow(() -> new AppException(ErrorCode.TABLE_AREA_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(area.getBranchId());
        return restaurantTableRepository.findByAreaIdOrderByTableNumberAsc(areaId)
                .stream()
                .filter(table -> table.getStatus() != RestaurantTableStatus.DELETED)
                .map(mapper::toTableResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public RestaurantTableResponse getTable(String tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_TABLE_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(table.getArea().getBranchId());
        return mapper.toTableResponse(table);
    }

    @Override
    @Transactional
    public RestaurantTableResponse confirmReservation(String tableId) {
        RestaurantTable table = getBookingTable(tableId);
        if (table.getStatus() == RestaurantTableStatus.OCCUPIED
                || tableSessionRepository.existsByTableIdAndStatus(tableId, TableSessionStatus.ACTIVE)) {
            throw new AppException(ErrorCode.TABLE_SESSION_ACTIVE_EXISTS);
        }
        Booking booking = getActiveBooking(tableId);

        booking.setStatus(BookingStatus.SEATED);
        table.setStatus(RestaurantTableStatus.OCCUPIED);
        bookingRepository.save(booking);
        restaurantTableRepository.save(table);
        tableSessionRepository.save(TableSession.builder()
                .branchId(table.getArea().getBranchId())
                .table(table)
                .guestName("Khách đặt bàn")
                .guestPhone(booking.getCustomer().getPhone())
                .partySize(booking.getGuestCount() != null ? booking.getGuestCount() : 1)
                .status(TableSessionStatus.ACTIVE)
                .startedAt(Instant.now())
                .note(booking.getNote() == null ? null
                        : booking.getNote().substring(
                                0,
                                Math.min(booking.getNote().length(), TableSessionConstants.MAX_NOTE_LENGTH)))
                .build());
        return mapper.toTableResponse(table);
    }

    @Override
    @Transactional
    public RestaurantTableResponse cancelReservation(String tableId) {
        RestaurantTable table = getBookingTable(tableId);
        Booking booking = getActiveBooking(tableId);
        booking.setStatus(BookingStatus.CANCELLED);
        if (table.getStatus() != RestaurantTableStatus.OCCUPIED) {
            table.setStatus(RestaurantTableStatus.AVAILABLE);
        }
        bookingRepository.save(booking);
        restaurantTableRepository.save(table);
        return mapper.toTableResponse(table);
    }

    private RestaurantTable getBookingTable(String tableId) {
        RestaurantTable table = restaurantTableRepository.findById(tableId)
                .orElseThrow(() -> new AppException(ErrorCode.RESTAURANT_TABLE_NOT_FOUND));
        tableBranchGuard.validateBranchAccess(table.getArea().getBranchId());
        if (table.getStatus() == RestaurantTableStatus.DELETED) {
            throw new AppException(ErrorCode.TABLE_NOT_AVAILABLE);
        }
        return table;
    }

    private Booking getActiveBooking(String tableId) {
        return bookingRepository.findFirstByTables_IdAndStatusInOrderByBookingTimeAsc(
                        tableId,
                        List.of(BookingStatus.PENDING, BookingStatus.CONFIRMED)
                )
                .orElseThrow(() -> new AppException(ErrorCode.BOOKING_NOT_FOUND));
    }

}
