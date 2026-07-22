package com.restaurant.crm.modules.identity.constants.permission;

public class PermissionConstants {
    private PermissionConstants() {}

    //======= Permission table name ========
    public static final String TABLE_PERMISSION = "system_permissions";

    //======= Permission column name ========
    public static final String COL_PERMISSION_NAME = "permission_name";

    //======= Permission column definition ========
    public static final String PERMISSION_NAME_DEFINITION = "VARCHAR(100)";

    //======= Permission validation values limit ========
    public static final int MIN_CHARS_PERMISSION_NAME = 1;
    public static final int MAX_CHARS_PERMISSION_NAME = 100;
}
