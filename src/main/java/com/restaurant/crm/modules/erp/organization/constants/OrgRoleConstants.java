package com.restaurant.crm.modules.erp.organization.constants;

public class OrgRoleConstants {
    private OrgRoleConstants() {}

    //======= Predefined role names ========
    public static final String OWNER_ROLE = "OWNER";

    //======= Role table name ========
    public static final String TABLE_ROLE = "org_roles";

    //======= Role column name ========
    public static final String COL_ROLE_NAME = "role_name";
    public static final String COL_DATA_SCOPE = "data_scope";

    //======= Role column definition ========
    public static final String ROLE_NAME_DEFINITION = "VARCHAR(50)";

    //======= Role validation values limit ========
    public static final int MIN_CHARS_ROLE_NAME = 1;
    public static final int MAX_CHARS_ROLE_NAME = 50;
}
