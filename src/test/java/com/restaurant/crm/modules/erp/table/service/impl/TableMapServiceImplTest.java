package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaMapResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableMapResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableStatusResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.entity.TableArea;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableMapMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.erp.table.repository.TableAreaRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableMapServiceImplTest {

    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    TableAreaRepository tableAreaRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    TableMapMapper tableMapMapper;

    TableMapServiceImpl tableMapService;

    @BeforeEach
    void setUp() {
        tableMapService = new TableMapServiceImpl(
                organizationBranchRepository,
                tableAreaRepository,
                restaurantTableRepository,
                tableMapMapper
        );
    }

    @Test
    void getTableMap_groupsTablesByAreaForAuthenticatedBranch() {
        OrganizationBranch branch = activeBranch();
        TableArea indoor = TableArea.builder().id("area-1").branchId("branch-1").build();
        RestaurantTable table = RestaurantTable.builder().id("table-1").area(indoor).build();
        RestaurantTable deletedTable = RestaurantTable.builder()
                .id("table-2")
                .area(indoor)
                .status(RestaurantTableStatus.DELETED)
                .build();
        TableAreaMapResponse areaResponse = TableAreaMapResponse.builder().id("area-1").build();
        TableStatusResponse tableResponse = TableStatusResponse.builder().id("table-1").build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(branch));
            when(tableAreaRepository.findByBranchIdOrderByDisplayOrderAscAreaNameAsc("branch-1"))
                    .thenReturn(List.of(indoor));
            when(restaurantTableRepository
                    .findByAreaBranchIdOrderByAreaAreaNameAscTableNumberAsc("branch-1"))
                    .thenReturn(List.of(table, deletedTable));
            when(tableMapMapper.toAreaResponse(indoor)).thenReturn(areaResponse);
            when(tableMapMapper.toTableResponse(table)).thenReturn(tableResponse);

            TableMapResponse response = tableMapService.getTableMap(null);

            assertEquals("branch-1", response.getBranchId());
            assertEquals(1, response.getAreas().size());
            assertEquals(List.of(tableResponse), response.getAreas().getFirst().getTables());
        }
    }

    @Test
    void getTableMap_filtersOneAreaInsideAuthenticatedBranch() {
        TableArea area = TableArea.builder().id("area-1").branchId("branch-1").build();
        TableAreaMapResponse areaResponse = TableAreaMapResponse.builder().id("area-1").build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(activeBranch()));
            when(tableAreaRepository.findByIdAndBranchId("area-1", "branch-1"))
                    .thenReturn(Optional.of(area));
            when(restaurantTableRepository.findByAreaIdOrderByTableNumberAsc("area-1"))
                    .thenReturn(List.of());
            when(tableMapMapper.toAreaResponse(area)).thenReturn(areaResponse);

            tableMapService.getTableMap("area-1");

            verify(tableAreaRepository).findByIdAndBranchId("area-1", "branch-1");
        }
    }

    @Test
    void getTableMap_rejectsAreaFromAnotherBranch() {
        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(activeBranch()));
            when(tableAreaRepository.findByIdAndBranchId("foreign-area", "branch-1"))
                    .thenReturn(Optional.empty());

            AppException exception = assertThrows(
                    AppException.class,
                    () -> tableMapService.getTableMap("foreign-area")
            );
            assertEquals(ErrorCode.TABLE_AREA_NOT_FOUND, exception.getErrorCode());
        }
    }

    @Test
    void getTableMap_rejectsInactiveBranch() {
        OrganizationBranch branch = OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.INACTIVE)
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(branch));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> tableMapService.getTableMap(null)
            );
            assertEquals(ErrorCode.ORGANIZATION_BRANCH_INACTIVE, exception.getErrorCode());
        }
    }

    private OrganizationBranch activeBranch() {
        return OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.ACTIVE)
                .build();
    }
}
