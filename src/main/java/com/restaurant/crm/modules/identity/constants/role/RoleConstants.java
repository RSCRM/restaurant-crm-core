package com.restaurant.crm.modules.identity.constants.role;

public class RoleConstants {
    private RoleConstants() {}

    //======= Role table name ========
    public static final String TABLE_ROLE = "system_roles";
    public static final String TABLE_USER_ROLE = "user_system_roles";
    public static final String TABLE_ROLE_PERMISSION = "system_role_permissions";

    //======= Role column name ========
    public static final String COL_ROLE_NAME = "role_name";
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ROLE_ID = "system_role_id";
    public static final String COL_PERMISSION_ID = "system_permission_id";

    //======= Role column definition ========
    public static final String ROLE_NAME_DEFINITION = "VARCHAR(50)";

    //======= Role validation values limit ========
    public static final int MIN_CHARS_ROLE_NAME = 1;
    public static final int MAX_CHARS_ROLE_NAME = 50;
}
