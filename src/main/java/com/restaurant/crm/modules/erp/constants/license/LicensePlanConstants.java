package com.restaurant.crm.modules.erp.constants.license;

public class LicensePlanConstants {
    private LicensePlanConstants() {
    }

    public static final String TABLE_LICENSE_PLAN = "license_plans";

    public static final String COL_PLAN_NAME = "plan_name";
    public static final String COL_DESCRIPTION = "description";
    public static final String COL_PRICE = "price";
    public static final String COL_MAX_BRANCHES = "max_branches";
    public static final String COL_DURATION_DAYS = "duration_days";
    public static final String COL_ENABLED = "enabled";

    public static final String PLAN_NAME_DEFINITION = "VARCHAR(100)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MIN_CHARS_PLAN_NAME = 1;
    public static final int MAX_CHARS_PLAN_NAME = 100;
}
