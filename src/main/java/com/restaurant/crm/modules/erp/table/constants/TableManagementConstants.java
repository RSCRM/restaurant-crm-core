package com.restaurant.crm.modules.erp.table.constants;

import com.restaurant.crm.modules.erp.table.enums.RestaurantTableStatus;

public final class TableManagementConstants {
    private TableManagementConstants() {}

    public static final RestaurantTableStatus DEFAULT_STATUS = RestaurantTableStatus.AVAILABLE;

    public static final String BASE_PATH = "/api/v1/erp";
    public static final String AREAS = "/table-areas";
    public static final String TABLES = "/restaurant-tables";
}
