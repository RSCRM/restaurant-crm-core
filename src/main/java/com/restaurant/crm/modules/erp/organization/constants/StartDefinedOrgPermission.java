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

    // Kitchen Display (KDS) Permissions — chef actions
    public static final String KITCHEN_ORDER_READ = "KITCHEN_ORDER_READ";
    public static final String KITCHEN_ITEM_ACCEPT = "KITCHEN_ITEM_ACCEPT";
    public static final String KITCHEN_ITEM_UPDATE = "KITCHEN_ITEM_UPDATE";
    public static final String KITCHEN_ITEM_RELEASE = "KITCHEN_ITEM_RELEASE";
    public static final String KITCHEN_ITEM_CANCEL = "KITCHEN_ITEM_CANCEL";

    // Service (waiter) item-flow Permissions
    public static final String SERVICE_ITEM_READY = "SERVICE_ITEM_READY";
    public static final String SERVICE_ITEM_SERVE = "SERVICE_ITEM_SERVE";

    // Payment Permissions
    public static final String PAYMENT_READ = "PAYMENT_READ";
    public static final String PAYMENT_CREATE = "PAYMENT_CREATE";

    // Operations & Setup Permissions
    public static final String MENU_MANAGE = "MENU_MANAGE";
    public static final String TABLE_MANAGE = "TABLE_MANAGE";
    public static final String TABLE_MAP_READ = "TABLE_MAP_READ";
    public static final String TABLE_SEARCH_READ = "TABLE_SEARCH_READ";
    public static final String REPORT_VIEW = "REPORT_VIEW";

    // Management & Administration Permissions
    public static final String STAFF_MANAGE = "STAFF_MANAGE";
    public static final String BRANCH_MANAGE = "BRANCH_MANAGE";
    public static final String ORG_MANAGE = "ORG_MANAGE";
}
