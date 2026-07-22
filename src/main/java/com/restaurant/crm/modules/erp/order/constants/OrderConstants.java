package com.restaurant.crm.modules.erp.order.constants;

public class OrderConstants {
    private OrderConstants() {}

    public static final String TABLE_ORDER = "orders";
<<<<<<< HEAD
    public static final String TABLE_ORDER_ITEM = "order_items";
    public static final String TABLE_ORDER_ITEM_MODIFIER = "order_item_modifiers";
=======
>>>>>>> 974a648899522665abe78a764198d9de7c28cde8

    public static final String UK_BRANCH_ORDER_CODE = "uk_orders_branch_order_code";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_TABLE_ID = "table_id";
    public static final String COL_RESERVATION_ID = "reservation_id";
    public static final String COL_ORDER_CODE = "order_code";
    public static final String COL_ORDER_TYPE = "order_type";
    public static final String COL_STATUS = "status";
    public static final String COL_CUSTOMER_NAME = "customer_name";
    public static final String COL_CUSTOMER_PHONE = "customer_phone";
    public static final String COL_NOTE = "note";
    public static final String COL_SUBTOTAL = "subtotal";
    public static final String COL_DISCOUNT_AMOUNT = "discount_amount";
    public static final String COL_TOTAL_AMOUNT = "total_amount";
    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_PRODUCT_ID = "product_id";
    public static final String COL_COMBO_ID = "combo_id";
    public static final String COL_QUANTITY = "quantity";
    public static final String COL_UNIT_PRICE = "unit_price";
    public static final String COL_ORDER_ITEM_ID = "order_item_id";
    public static final String COL_MODIFIER_OPTION_ID = "modifier_option_id";
    public static final String COL_ADDITIONAL_PRICE = "additional_price";

    public static final String UUID_DEFINITION = "VARCHAR(36)";
    public static final String ORDER_CODE_DEFINITION = "VARCHAR(30)";
    public static final String ENUM_DEFINITION = "VARCHAR(20)";
    public static final String CUSTOMER_NAME_DEFINITION = "VARCHAR(100)";
    public static final String CUSTOMER_PHONE_DEFINITION = "VARCHAR(20)";
    public static final String NOTE_DEFINITION = "VARCHAR(255)";
    public static final String ORDER_CODE_PREFIX = "ORD-";

    public static final int MIN_QUANTITY = 1;
    public static final int ORDER_CODE_RANDOM_LENGTH = 8;
    public static final int MAX_CHARS_ORDER_CODE = 30;
    public static final int MAX_CHARS_CUSTOMER_NAME = 100;
    public static final int MAX_CHARS_CUSTOMER_PHONE = 20;
    public static final int MAX_CHARS_NOTE = 255;

    public static final int MONEY_PRECISION = 10;
    public static final int MONEY_SCALE = 2;
}
