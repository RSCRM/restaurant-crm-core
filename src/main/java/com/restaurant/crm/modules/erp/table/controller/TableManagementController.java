package com.restaurant.crm.modules.erp.table.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.erp.table.constants.TableManagementConstants;
import com.restaurant.crm.modules.erp.table.constants.permission.TablePermissionConstants;
import com.restaurant.crm.modules.erp.table.dto.request.CreateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.CreateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.response.RestaurantTableResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaResponse;
import com.restaurant.crm.modules.erp.table.service.interfaces.TableManagementService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping(TableManagementConstants.BASE_PATH)
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableManagementController {

    TableManagementService tableManagementService;

    // ---------- AREA ----------

    @PostMapping(TableManagementConstants.AREAS)
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.TABLE_AREA_ADD + "')")
    public ResponseEntity<ApiResponse<TableAreaResponse>> createArea(@Valid @RequestBody CreateAreaRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<TableAreaResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.createArea(request)).build());
    }

    @PutMapping(TableManagementConstants.AREAS + "/{id}")
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.TABLE_AREA_UPDATE + "')")
    public ResponseEntity<ApiResponse<TableAreaResponse>> updateArea(@PathVariable String id,
                                                                     @Valid @RequestBody UpdateAreaRequest request) {
        return ResponseEntity.ok(ApiResponse.<TableAreaResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.updateArea(id, request)).build());
    }

    @DeleteMapping(TableManagementConstants.AREAS + "/{id}")
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.TABLE_AREA_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteArea(@PathVariable String id) {
        tableManagementService.deleteArea(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @GetMapping(TableManagementConstants.AREAS)
    public ResponseEntity<ApiResponse<List<TableAreaResponse>>> listAreas(@RequestParam String branchId) {
        return ResponseEntity.ok(ApiResponse.<List<TableAreaResponse>>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.listAreasByBranch(branchId)).build());
    }

    @GetMapping(TableManagementConstants.AREAS + "/{id}")
    public ResponseEntity<ApiResponse<TableAreaResponse>> getArea(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<TableAreaResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.getArea(id)).build());
    }

    // ---------- TABLE ----------

    @PostMapping(TableManagementConstants.TABLES)
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.RESTAURANT_TABLE_ADD + "')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> createTable(@Valid @RequestBody CreateTableRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.<RestaurantTableResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.createTable(request)).build());
    }

    @PutMapping(TableManagementConstants.TABLES + "/{id}")
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.RESTAURANT_TABLE_UPDATE + "')")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> updateTable(@PathVariable String id,
                                                                            @Valid @RequestBody UpdateTableRequest request) {
        return ResponseEntity.ok(ApiResponse.<RestaurantTableResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.updateTable(id, request)).build());
    }

    @DeleteMapping(TableManagementConstants.TABLES + "/{id}")
    @PreAuthorize("@tableAccessChecker.canManage('" + TablePermissionConstants.RESTAURANT_TABLE_DELETE + "')")
    public ResponseEntity<ApiResponse<Void>> deleteTable(@PathVariable String id) {
        tableManagementService.deleteTable(id);
        return ResponseEntity.ok(ApiResponse.<Void>builder().success(ApiConstant.SUCCESS).build());
    }

    @GetMapping(TableManagementConstants.TABLES)
    public ResponseEntity<ApiResponse<List<RestaurantTableResponse>>> listTables(@RequestParam String areaId) {
        return ResponseEntity.ok(ApiResponse.<List<RestaurantTableResponse>>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.listTablesByArea(areaId)).build());
    }

    @GetMapping(TableManagementConstants.TABLES + "/{id}")
    public ResponseEntity<ApiResponse<RestaurantTableResponse>> getTable(@PathVariable String id) {
        return ResponseEntity.ok(ApiResponse.<RestaurantTableResponse>builder()
                .success(ApiConstant.SUCCESS).data(tableManagementService.getTable(id)).build());
    }
}
