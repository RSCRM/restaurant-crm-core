package com.restaurant.crm.modules.erp.constants.organization_branch;

public class OrganizationBranchConstants {
    private OrganizationBranchConstants() {
    }

    public static final String TABLE_ORGANIZATION_BRANCH = "organization_branches";

    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_MANAGER_ID = "manager_id";
    public static final String COL_BRANCH_NAME = "branch_name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PHONE = "phone";
    public static final String COL_STATUS = "status";

    public static final String BRANCH_NAME_DEFINITION = "VARCHAR(100)";
    public static final String ADDRESS_DEFINITION = "VARCHAR(255)";
    public static final String PHONE_DEFINITION = "VARCHAR(20)";

    public static final int MIN_CHARS_BRANCH_NAME = 1;
    public static final int MAX_CHARS_BRANCH_NAME = 100;
}
