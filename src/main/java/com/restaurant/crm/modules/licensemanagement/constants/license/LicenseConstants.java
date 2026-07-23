package com.restaurant.crm.modules.licensemanagement.constants.license;

public class LicenseConstants {
    private LicenseConstants() {}

    //======= License table name ========
    public static final String TABLE_LICENSE = "license";

    //======= License column name ========
    public static final String COL_CODE = "code";
    public static final String COL_NAME = "name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PRICE = "price";
    public static final String COL_BILLING_CYCLE = "billing_cycle";
    public static final String COL_MAX_BRANCH = "max_branch";
    public static final String COL_MAX_EMPLOYEE = "max_employee";
    public static final String COL_STATUS = "status";
    public static final String COL_DELETED_AT = "deleted_at";

    //======= License column definition ========
    public static final String CODE_DEFINITION = "VARCHAR(50)";
    public static final String NAME_DEFINITION = "VARCHAR(255)";
    public static final String BILLING_CYCLE_DEFINITION = "VARCHAR(20)";
    public static final String STATUS_DEFINITION = "VARCHAR(20)";

    //======= License validation values limit ========
    public static final int MAX_CODE = 50;
    public static final int MAX_NAME = 255;
    public static final int MAX_DESCRIPTION = 2000;
    public static final int MIN_MAX_BRANCH = -1;
    public static final int MIN_MAX_EMPLOYEE = -1;
}
