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

    // ===== ORGANIZATION =====
    public static final String ORGANIZATION_VIEW = "ORGANIZATION_VIEW";
    public static final String ORGANIZATION_MANAGE = "ORGANIZATION_MANAGE";

    // ===== ORGANIZATION BRANCH =====
    public static final String ORGANIZATION_BRANCH_VIEW = "ORGANIZATION_BRANCH_VIEW";
    public static final String ORGANIZATION_BRANCH_MANAGE = "ORGANIZATION_BRANCH_MANAGE";

    // ===== INGREDIENT CATEGORY =====
    public static final String INGREDIENT_CATEGORY_VIEW = "INGREDIENT_CATEGORY_VIEW";
    public static final String INGREDIENT_CATEGORY_MANAGE = "INGREDIENT_CATEGORY_MANAGE";
    public static final String INGREDIENT_VIEW = "INGREDIENT_CATEGORY_MANAGE";
    public static final String INGREDIENT_MANAGE = "INGREDIENT_CATEGORY_MANAGE";

    // ===== INVENTORY =====
    public static final String INVENTORY_VIEW = "INVENTORY_VIEW";
    public static final String INVENTORY_MANAGE = "INVENTORY_MANAGE";
    public static final String INVENTORY_TRANSACTION_VIEW = "INVENTORY_TRANSACTION_VIEW";
    public static final String INVENTORY_TRANSACTION_MANAGE = "INVENTORY_TRANSACTION_MANAGE";

    // ===== CONTRACT =====
    public static final String CONTRACT_LICENSE_VIEW = "CONTRACT_LICENSE_VIEW";

    private StartDefinedPermission() {
    }
}
