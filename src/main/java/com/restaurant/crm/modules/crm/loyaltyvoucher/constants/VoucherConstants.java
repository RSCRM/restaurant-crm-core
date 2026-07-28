package com.restaurant.crm.modules.crm.loyaltyvoucher.constants;

public class VoucherConstants {
    private VoucherConstants() {}

    public static final String TABLE_VOUCHER = "vouchers";

    public static final String COL_RESTAURANT_ID = "restaurant_id";
    public static final String COL_TITLE = "title";
    public static final String COL_DISCOUNT_PERCENT = "discount_percent";
    public static final String COL_MIN_BILL_AMOUNT = "min_bill_amount";
    public static final String COL_POINTS_REQUIRED = "points_required";
    public static final String COL_IS_ACTIVE = "is_active";
    public static final String COL_EXPIRED_AT = "expired_at";

    public static final String TITLE_DEFINITION = "VARCHAR(255)";
    public static final String MIN_BILL_AMOUNT_DEFINITION = "DECIMAL(15,2)";

    public static final int MIN_DISCOUNT_PERCENT = 1;
    public static final int MAX_DISCOUNT_PERCENT = 100;
}
