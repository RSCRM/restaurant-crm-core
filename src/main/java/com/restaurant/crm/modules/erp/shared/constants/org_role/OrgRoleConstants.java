package com.restaurant.crm.modules.erp.shared.constants.org_role;

public class OrgRoleConstants {
    private OrgRoleConstants() {
    }

    public static final String TABLE_ORG_ROLE = "org_roles";
    public static final String TABLE_ORG_ROLE_PERMISSION = "org_role_permissions";

    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_ROLE_ID = "org_role_id";
    public static final String COL_PERMISSION_ID = "org_permission_id";
    public static final String COL_ROLE_NAME = "role_name";
    public static final String COL_DESCRIPTION = "description";

    public static final String ROLE_NAME_DEFINITION = "VARCHAR(50)";
    public static final String DESCRIPTION_DEFINITION = "VARCHAR(255)";

    public static final int MIN_CHARS_ROLE_NAME = 1;
    public static final int MAX_CHARS_ROLE_NAME = 50;
}
