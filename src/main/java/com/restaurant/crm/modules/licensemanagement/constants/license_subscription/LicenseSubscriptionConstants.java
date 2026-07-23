package com.restaurant.crm.modules.licensemanagement.constants.license_subscription;

public class LicenseSubscriptionConstants {
    private LicenseSubscriptionConstants() {}

    //======= License Subscription table name ========
    public static final String TABLE_LICENSE_SUBSCRIPTION = "license_subscription";

    //======= License Subscription column name ========
    public static final String COL_LICENSE_ID = "license_id";
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_STATUS = "status";
    public static final String COL_PRICE = "price";
    public static final String COL_BILLING_CYCLE = "billing_cycle";
    public static final String COL_MAX_BRANCH = "max_branch";
    public static final String COL_MAX_EMPLOYEE = "max_employee";

    //======= License Subscription column definition ========
    public static final String STATUS_DEFINITION = "VARCHAR(20)";
    public static final String BILLING_CYCLE_DEFINITION = "VARCHAR(20)";
}
