package com.restaurant.crm.modules.erp.inventory.constants;

public final class InventoryConstants {

    private InventoryConstants() {
    }

    public static final String TABLE_INVENTORY = "inventories";
    public static final String COL_INGREDIENT_ID = "ingredient_id";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_MINIMUM_QUANTITY = "minimum_quantity";
    public static final String COL_STATUS = "status";

    public static final String QUANTITY_DEFINITION = "DECIMAL(12,3)";
    public static final String STATUS_DEFINITION = "VARCHAR(20)";
}
