package com.restaurant.crm.modules.erp.organization.constants;

public class OrganizationConstants {
    private OrganizationConstants() {}

    //======= Organization table name ========
    public static final String TABLE_ORGANIZATION = "organizations";

    //======= Organization column name ========
    public static final String COL_ORGANIZATION_NAME = "organization_name";
    public static final String COL_TAX_CODE = "tax_code";
    public static final String COL_ADDRESS = "address";
    public static final String COL_PHONE = "phone";
    public static final String COL_EMAIL = "email";
    public static final String COL_STATUS = "status";

    //======= Organization column definition ========
    public static final String ORGANIZATION_NAME_DEFINITION = "VARCHAR(150)";
    public static final String TAX_CODE_DEFINITION = "VARCHAR(50)";
    public static final String ADDRESS_DEFINITION = "VARCHAR(255)";
    public static final String PHONE_DEFINITION = "VARCHAR(20)";
    public static final String EMAIL_DEFINITION = "VARCHAR(100)";

    //======= Organization validation values limit ========
    public static final int MAX_CHARS_ORGANIZATION_NAME = 150;
    public static final int MAX_CHARS_TAX_CODE = 50;
    public static final int MAX_CHARS_ADDRESS = 255;
    public static final int MAX_CHARS_PHONE = 20;
    public static final int MAX_CHARS_EMAIL = 100;
}
