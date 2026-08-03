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
    public static final String TABLE_MAP_READ = "TABLE_MAP_READ";
    public static final String TABLE_SEARCH_READ = "TABLE_SEARCH_READ";
    public static final String TABLE_SESSION_CREATE = "TABLE_SESSION_CREATE";
    public static final String REPORT_VIEW = "REPORT_VIEW";

    // Management & Administration Permissions
    public static final String STAFF_MANAGE = "STAFF_MANAGE";
    public static final String BRANCH_MANAGE = "BRANCH_MANAGE";
    public static final String BRANCH_MANAGER_ASSIGN = "BRANCH_MANAGER_ASSIGN";
    public static final String ORG_MANAGE = "ORG_MANAGE";

    // ===== ORGANIZATION =====
    public static final String ORGANIZATION_VIEW = "ORGANIZATION_VIEW";
    public static final String ORGANIZATION_MANAGE = "ORGANIZATION_MANAGE";

    // ===== ORGANIZATION BRANCH =====
    public static final String ORGANIZATION_BRANCH_VIEW = "ORGANIZATION_BRANCH_VIEW";
    public static final String ORGANIZATION_BRANCH_MANAGE = "ORGANIZATION_BRANCH_MANAGE";

    // ===== INGREDIENT CATEGORY =====
    public static final String INGREDIENT_CATEGORY_VIEW = "INGREDIENT_CATEGORY_VIEW";
    public static final String INGREDIENT_CATEGORY_MANAGE = "INGREDIENT_CATEGORY_MANAGE";
    public static final String INGREDIENT_VIEW = "INGREDIENT_VIEW";
    public static final String INGREDIENT_MANAGE = "INGREDIENT_MANAGE";

    // ===== INVENTORY =====
    public static final String INVENTORY_VIEW = "INVENTORY_VIEW";
    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE";
    public static final String INVENTORY_TRANSACTION_VIEW = "INVENTORY_TRANSACTION_VIEW";
    public static final String INVENTORY_TRANSACTION_MANAGE = "INVENTORY_TRANSACTION_MANAGE";
}
