package com.restaurant.crm.modules.crm.customeraccount.constants;

public class CustomerConstants {
    private CustomerConstants() {}

    public static final String TABLE_CUSTOMER = "customers";

    public static final String COL_PHONE = "phone";
    public static final String COL_STATUS = "status";

    public static final String PHONE_DEFINITION = "VARCHAR(15)";

    public static final int MIN_CHARS_PHONE = 9;
    public static final int MAX_CHARS_PHONE = 15;

    //error
    public static final String CUSTOMER_STATUS_REQUIRE = "CUSTOMER_STATUS_REQUIRE";
}
