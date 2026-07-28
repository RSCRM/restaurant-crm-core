package com.restaurant.crm.modules.erp.organization.constants;

/**
 * Constants for predefined organization-level permissions (org_permissions).
 */
public class StartDefinedOrgPermission {
    private StartDefinedOrgPermission() {}

    // Order Permissions
    public static final String ORDER_READ = "ORDER_READ";
    public static final String ORDER_CREATE = "ORDER_CREATE";
    public static final String ORDER_UPDATE = "ORDER_UPDATE";
    public static final String ORDER_DELETE = "ORDER_DELETE";

    // Payment Permissions
    public static final String PAYMENT_READ = "PAYMENT_READ";
    public static final String PAYMENT_CREATE = "PAYMENT_CREATE";

    // Operations & Setup Permissions
    public static final String MENU_MANAGE = "MENU_MANAGE";
    public static final String TABLE_MANAGE = "TABLE_MANAGE";
    public static final String REPORT_VIEW = "REPORT_VIEW";

    // Management & Administration Permissions
    public static final String STAFF_MANAGE = "STAFF_MANAGE";
    public static final String BRANCH_MANAGE = "BRANCH_MANAGE";
    public static final String ORG_MANAGE = "ORG_MANAGE";

    // Branch Manager Permissions
    public static final String BRANCH_MANAGER_VIEW = "BRANCH_MANAGER_VIEW";
    public static final String BRANCH_MANAGER_UPDATE = "BRANCH_MANAGER_UPDATE";
    public static final String BRANCH_MANAGER_DELETE = "BRANCH_MANAGER_DELETE";
}
