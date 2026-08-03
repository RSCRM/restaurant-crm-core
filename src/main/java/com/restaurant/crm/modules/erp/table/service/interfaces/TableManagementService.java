package com.restaurant.crm.modules.erp.table.service.interfaces;

import com.restaurant.crm.modules.erp.table.dto.request.CreateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.CreateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateAreaRequest;
import com.restaurant.crm.modules.erp.table.dto.request.UpdateTableRequest;
import com.restaurant.crm.modules.erp.table.dto.response.RestaurantTableResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableAreaResponse;

import java.util.List;

public interface TableManagementService {
    TableAreaResponse createArea(CreateAreaRequest request);
    TableAreaResponse updateArea(String areaId, UpdateAreaRequest request);
    void deleteArea(String areaId);
    List<TableAreaResponse> listAreasByBranch(String branchId);
    TableAreaResponse getArea(String areaId);

    RestaurantTableResponse createTable(CreateTableRequest request);
    RestaurantTableResponse updateTable(String tableId, UpdateTableRequest request);
    void deleteTable(String tableId);
    List<RestaurantTableResponse> listTablesByArea(String areaId);
    RestaurantTableResponse getTable(String tableId);
    RestaurantTableResponse confirmReservation(String tableId);
    RestaurantTableResponse cancelReservation(String tableId);
}
