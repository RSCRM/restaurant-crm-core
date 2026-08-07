package com.restaurant.crm.modules.erp.organization.constants;

public class OrganizationBranchConstants {
    private OrganizationBranchConstants() {}

    //======= OrganizationBranch table name ========
    public static final String TABLE_ORGANIZATION_BRANCH = "organization_branches";

    //======= OrganizationBranch column name ========
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_BRANCH_NAME = "branch_name";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PHONE = "phone";
    public static final String COL_STATUS = "status";

    //======= OrganizationBranch column definition ========
    public static final String BRANCH_NAME_DEFINITION = "VARCHAR(100)";
    public static final String ADDRESS_DEFINITION = "VARCHAR(255)";
    public static final String PHONE_DEFINITION = "VARCHAR(20)";

    //======= OrganizationBranch validation values limit ========
    public static final int MAX_CHARS_BRANCH_NAME = 100;
    public static final int MAX_CHARS_ADDRESS = 255;
    public static final int MAX_CHARS_PHONE = 20;
}
