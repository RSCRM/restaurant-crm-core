package com.restaurant.crm.modules.crm.loyalty_voucher.constants;

public class CustomerVoucherConstants {
    private CustomerVoucherConstants() {}

    public static final String TABLE_CUSTOMER_VOUCHER = "customer_vouchers";

    public static final String COL_CUSTOMER_ID = "customer_id";
    public static final String COL_RESTAURANT_ID = "restaurant_id";
    public static final String COL_VOUCHER_ID = "voucher_id";
    public static final String COL_VOUCHER_SN = "voucher_sn";
    public static final String COL_STATUS = "status";
    public static final String COL_USED_AT = "used_at";
    public static final String COL_ORDER_ID = "order_id";

    public static final String VOUCHER_SN_DEFINITION = "VARCHAR(100)";
}
