package com.restaurant.crm.modules.erp.constants;

public class OrderItemConstants {
    private OrderItemConstants() {}

    public static final String TABLE_ORDER_ITEM = "order_items";

    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_DISH_NAME = "dish_name";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_NOTE = "note";

    // Status-lifecycle columns (uc-scf-01..06)
    public static final String COL_STATUS = "status";
    public static final String COL_PREPARED_BY = "prepared_by";
    public static final String COL_STARTED_AT = "started_at";
    public static final String COL_COMPLETED_AT = "completed_at";
    public static final String COL_CANCEL_REASON = "cancel_reason";
    public static final String COL_PRIORITY_FLAG = "priority_flag";

    // Inherited from BaseEntity; referenced here for the kitchen board index.
    public static final String COL_CREATED_AT = "created_at";

    public static final String IDX_KITCHEN_BOARD = "idx_order_items_kitchen_board";
    public static final String IDX_ORDER_ID = "idx_order_items_order_id";

    public static final String DISH_NAME_DEFINITION = "VARCHAR(255)";
    public static final String NOTE_DEFINITION = "VARCHAR(255)";
    public static final String PREPARED_BY_DEFINITION = "VARCHAR(36)";
    public static final String CANCEL_REASON_DEFINITION = "VARCHAR(255)";
}
