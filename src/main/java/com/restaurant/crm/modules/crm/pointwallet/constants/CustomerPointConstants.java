package com.restaurant.crm.modules.crm.pointwallet.constants;

public class CustomerPointConstants {
    private CustomerPointConstants() {}

    public static final String TABLE_CUSTOMER_POINT = "customer_point";

    public static final String COL_CUSTOMER_ID = "customer_id";
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_CURRENT_POINTS = "current_points";
    public static final String COL_LIFETIME_POINTS = "lifetime_points";

    //error
    public static final String CUSTOMER_STATUS_REQUIRE = "CUSTOMER_STATUS_REQUIRE";
    public static final String ORGANIZATION_ID_REQUIRED ="ORGANIZATION_ID_REQUIRED";
}
