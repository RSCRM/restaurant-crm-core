package com.restaurant.crm.modules.erp.table.service.interfaces;

import com.restaurant.crm.common.dto.response.PagingResponse;
import com.restaurant.crm.modules.erp.table.dto.response.TableSearchResponse;
import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;

public interface TableSearchService {

    PagingResponse<TableSearchResponse> searchTables(
            String keyword,
            String areaId,
            RestaurantTableStatus status,
            Integer minCapacity,
            Integer maxCapacity,
            int page,
            int size
    );
}

