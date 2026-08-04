package com.restaurant.crm.modules.erp.inventory.constants;

public final class InventoryConstants {

    private InventoryConstants() {
    }

    public static final String TABLE_INVENTORY = "inventories";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_MINIMUM_QUANTITY = "minimum_quantity";
    public static final String COL_STATUS = "status";

    public static final String QUANTITY_DEFINITION = "DECIMAL(12,3)";
    public static final String STATUS_DEFINITION = "VARCHAR(20)";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_INVENTORY_CATEGORY_ID = "inventory_category_id";
    public static final String COL_INVENTORY_NAME = "inventory_name";
    public static final String COL_UNIT = "unit";
    public static final String COL_DESCRIPTION = "description";

    public static final String INVENTORY_NAME_DEFINITION = "VARCHAR(150)";
    public static final String UNIT_DEFINITION = "VARCHAR(20)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MAX_CHARS_INVENTORY_NAME = 150;
    public static final int MAX_CHARS_UNIT = 20;
    public static final int MAX_CHARS_DESCRIPTION = 255;
}
