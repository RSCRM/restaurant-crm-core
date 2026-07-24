package com.restaurant.crm.modules.erp.inventory.constants;

public final class InventoryTransactionConstants {

    private InventoryTransactionConstants() {
    }

    public static final String TABLE_INVENTORY_TRANSACTION = "inventory_transactions";
    public static final String COL_INVENTORY_ID = "inventory_id";
    public static final String COL_EMPLOYEE_ID = "employee_id";
    public static final String COL_TRANSACTION_TYPE = "transaction_type";
    public static final String COL_TRANSACTION_DIRECTION = "transaction_direction";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_NOTE = "note";
    public static final String COL_REFERENCE_ID = "reference_id";
    public static final String COL_TRANSACTION_TIME = "transaction_time";


    public static final String QUANTITY_DEFINITION = "DECIMAL(12,3)";
    public static final String NOTE_DEFINITION = "VARCHAR(255)";
    public static final String REFERENCE_ID_DEFINITION = "CHAR(36)";
}
