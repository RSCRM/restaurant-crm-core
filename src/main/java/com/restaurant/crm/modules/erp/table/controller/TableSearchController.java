package com.restaurant.crm.modules.erp.table.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.table.dto.response.TableSearchResponse;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableSearchService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/erp/tables")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableSearchController {

    TableSearchService tableSearchService;

    @GetMapping("/search")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.TABLE_SEARCH_READ + "')")
    public ResponseEntity<ApiResponse<PagingResponse<TableSearchResponse>>> searchTables(
            @RequestParam(required = false) String branchId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) RestaurantTableStatus status,
            @RequestParam(required = false) Integer minCapacity,
            @RequestParam(required = false) Integer maxCapacity,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        PagingResponse<TableSearchResponse> response = tableSearchService.searchTables(
                branchId,
                keyword,
                areaId,
                status,
                minCapacity,
                maxCapacity,
                page,
                size
        );

        return ResponseEntity.ok(ApiResponse.<PagingResponse<TableSearchResponse>>builder()
                .success(true)
                .data(response)
                .build());
    }
}

