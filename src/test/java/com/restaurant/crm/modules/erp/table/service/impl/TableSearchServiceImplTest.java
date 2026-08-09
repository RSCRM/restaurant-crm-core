package com.restaurant.crm.modules.erp.table.service.impl;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.organization.entity.OrganizationBranch;
import com.restaurant.crm.modules.erp.organization.enums.OrganizationBranchStatus;
import com.restaurant.crm.modules.erp.organization.repository.OrganizationBranchRepository;
import com.restaurant.crm.modules.erp.table.dto.response.TableSearchResponse;
import com.restaurant.crm.modules.erp.table.entity.RestaurantTable;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.mapper.TableSearchMapper;
import com.restaurant.crm.modules.erp.table.repository.RestaurantTableRepository;
import com.restaurant.crm.modules.identity.utils.AuthUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TableSearchServiceImplTest {

    @Mock
    OrganizationBranchRepository organizationBranchRepository;
    @Mock
    RestaurantTableRepository restaurantTableRepository;
    @Mock
    TableSearchMapper tableSearchMapper;

    TableSearchServiceImpl tableSearchService;

    @BeforeEach
    void setUp() {
        tableSearchService = new TableSearchServiceImpl(
                organizationBranchRepository,
                restaurantTableRepository,
                tableSearchMapper
        );
    }

    @Test
    void searchTables_returnsMappedPageForCombinedFilters() {
        RestaurantTable table = RestaurantTable.builder().id("table-1").build();
        TableSearchResponse mapped = TableSearchResponse.builder().id("table-1").build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findAll(
                    org.mockito.ArgumentMatchers.<Specification<RestaurantTable>>any(),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(table), PageRequest.of(0, 10), 1));
            when(tableSearchMapper.toResponse(table)).thenReturn(mapped);

            PagingResponse<TableSearchResponse> response = tableSearchService.searchTables(
                    "branch-1",
                    "A",
                    "area-1",
                    RestaurantTableStatus.AVAILABLE,
                    2,
                    6,
                    1,
                    10
            );

            assertEquals(1, response.getTotalElement());
            assertEquals(List.of(mapped), response.getData());
        }
    }

    @Test
    void searchTables_returnsEmptyPageWhenNoTableMatches() {
        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1"))
                    .thenReturn(Optional.of(activeBranch()));
            when(restaurantTableRepository.findAll(
                    org.mockito.ArgumentMatchers.<Specification<RestaurantTable>>any(),
                    any(Pageable.class)
            )).thenReturn(new PageImpl<>(List.of(), PageRequest.of(0, 10), 0));

            PagingResponse<TableSearchResponse> response = tableSearchService.searchTables(
                    "branch-1",
                    "not-found",
                    null,
                    null,
                    null,
                    null,
                    1,
                    10
            );

            assertEquals(0, response.getTotalElement());
            assertTrue(response.getData().isEmpty());
        }
    }

    @Test
    void searchTables_rejectsInvalidCapacityRange() {
        AppException exception = assertThrows(
                AppException.class,
                () -> tableSearchService.searchTables(
                        "branch-1",
                        null,
                        null,
                        null,
                        8,
                        4,
                        1,
                        10
                )
        );

        assertEquals(ErrorCode.TABLE_SEARCH_CRITERIA_INVALID, exception.getErrorCode());
    }

    @Test
    void searchTables_rejectsInactiveBranch() {
        OrganizationBranch branch = OrganizationBranch.builder()
                .id("branch-1")
                .status(OrganizationBranchStatus.INACTIVE)
                .build();

        try (MockedStatic<AuthUtils> authUtils = mockStatic(AuthUtils.class)) {
            authUtils.when(AuthUtils::getBranchId).thenReturn("branch-1");
            when(organizationBranchRepository.findById("branch-1")).thenReturn(Optional.of(branch));

            AppException exception = assertThrows(
                    AppException.class,
                    () -> tableSearchService.searchTables(
                            "branch-1",
                            null,
                            null,
                            null,
                            null,
                            null,
                            1,
                            10
                    )
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
