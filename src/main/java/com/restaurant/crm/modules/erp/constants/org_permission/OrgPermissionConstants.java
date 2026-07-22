package com.restaurant.crm.modules.erp.constants.org_permission;

public class OrgPermissionConstants {
    private OrgPermissionConstants() {
    }

    public static final String TABLE_ORG_PERMISSION = "org_permissions";

    public static final String COL_PERMISSION_CODE = "permission_code";
    public static final String COL_PERMISSION_NAME = "permission_name";
    public static final String COL_DESCRIPTION = "description";

    public static final String PERMISSION_CODE_DEFINITION = "VARCHAR(100)";
    public static final String PERMISSION_NAME_DEFINITION = "VARCHAR(100)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MIN_CHARS_PERMISSION_CODE = 1;
    public static final int MAX_CHARS_PERMISSION_CODE = 100;
}
