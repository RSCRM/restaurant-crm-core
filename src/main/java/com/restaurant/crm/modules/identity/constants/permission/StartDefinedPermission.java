package com.restaurant.crm.modules.identity.constants.permission;

public class StartDefinedPermission {
    // System-level Permissions
    public static final String SYSTEM_VIEW = "SYSTEM_VIEW";
    public static final String SYSTEM_MANAGE = "SYSTEM_MANAGE";

    public static final String ROLE_VIEW = "ROLE_VIEW";
    public static final String ROLE_MANAGE = "ROLE_MANAGE";

    public static final String PERMISSION_VIEW = "PERMISSION_VIEW";
    public static final String PERMISSION_MANAGE = "PERMISSION_MANAGE";

    public static final String AUDIT_VIEW = "AUDIT_VIEW";

    // User Permissions
    public static final String USER_VIEW = "USER_VIEW";
    public static final String USER_UPDATE = "USER_UPDATE";
    public static final String USER_DELETE = "USER_DELETE";

    // Branch Manager Permissions
    public static final String BRANCH_MANAGER_VIEW = "BRANCH_MANAGER_VIEW";
    public static final String BRANCH_MANAGER_CREATE = "BRANCH_MANAGER_CREATE";
    public static final String BRANCH_MANAGER_UPDATE = "BRANCH_MANAGER_UPDATE";
    public static final String BRANCH_MANAGER_DELETE = "BRANCH_MANAGER_DELETE";

    private StartDefinedPermission() {
    }
}
