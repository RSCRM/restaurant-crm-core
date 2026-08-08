package com.restaurant.crm.modules.erp.inventory.constants;

public final class InventoryCategoryConstants {

    private InventoryCategoryConstants() {}

    public static final String TABLE_INVENTORY_CATEGORY = "inventory_categories";
    public static final String COL_STATUS = "status";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_CATEGORY_NAME = "category_name";
    public static final String COL_DESCRIPTION = "description";

    public static final String CATEGORY_NAME_DEFINITION = "VARCHAR(100)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MAX_CHARS_CATEGORY_NAME = 100;
    public static final int MAX_CHARS_DESCRIPTION = 255;
    public static final String STATUS_DEFINITION = "varchar(20)";
}
