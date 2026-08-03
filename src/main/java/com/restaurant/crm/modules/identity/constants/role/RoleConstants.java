package com.restaurant.crm.modules.identity.constants.role;

public class RoleConstants {
    private RoleConstants() {}

    //======= Role table name ========
    public static final String TABLE_ROLE = "roles";

    //======= Role column name ========
    public static final String COL_ROLE_NAME = "role_name";
    public static final String COL_DATA_SCOPE = "data_scope";

    //======= Role column definition ========
    public static final String ROLE_NAME_DEFINITION = "VARCHAR(50)";

    //======= Role validation values limit ========
    public static final int MIN_CHARS_ROLE_NAME = 1;
    public static final int MAX_CHARS_ROLE_NAME = 50;
}
