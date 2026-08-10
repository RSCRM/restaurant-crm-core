package com.restaurant.crm.modules.erp.table.service.interfaces;

import com.restaurant.crm.modules.erp.table.dto.response.TableMapResponse;

public interface TableMapService {
    TableMapResponse getTableMap(String areaId, String branchId);
}

