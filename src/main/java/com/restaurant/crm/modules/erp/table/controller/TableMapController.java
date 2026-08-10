package com.restaurant.crm.modules.erp.table.controller;

import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.organization.constants.StartDefinedOrgPermission;
import com.restaurant.crm.modules.erp.table.dto.response.TableMapResponse;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableMapService;
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
public class TableMapController {

    TableMapService tableMapService;

    @GetMapping("/map")
    @PreAuthorize("hasAuthority('" + StartDefinedOrgPermission.TABLE_MAP_READ + "')")
    public ResponseEntity<ApiResponse<TableMapResponse>> getTableMap(
            @RequestParam(required = false) String areaId,
            @RequestParam(required = false) String branchId
    ) {
        return ResponseEntity.ok(ApiResponse.<TableMapResponse>builder()
                .success(true)
                .data(tableMapService.getTableMap(areaId, branchId))
                .build());
    }
}

