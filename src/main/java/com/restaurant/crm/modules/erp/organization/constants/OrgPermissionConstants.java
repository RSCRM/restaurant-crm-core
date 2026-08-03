package com.restaurant.crm.modules.erp.organization.constants;

public class OrgPermissionConstants {
    private OrgPermissionConstants() {}

    //======= Permission table name ========
    public static final String TABLE_PERMISSION = "org_permissions";

    //======= Permission column name ========
    public static final String COL_PERMISSION_NAME = "permission_name";

    //======= Permission column definition ========
    public static final String PERMISSION_NAME_DEFINITION = "VARCHAR(100)";

    //======= Permission validation values limit ========
    public static final int MIN_CHARS_PERMISSION_NAME = 1;
    public static final int MAX_CHARS_PERMISSION_NAME = 100;
}
