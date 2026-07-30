package com.restaurant.crm.modules.erp.organization.constants;

public class EmployeeConstants {
    private EmployeeConstants() {}

    //======= Employee table name ========
    public static final String TABLE_EMPLOYEE = "employees";

    //======= Employee column name ========
    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ROLE_ID = "org_role_id";
    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_FIRST_NAME = "first_name";
    public static final String COL_LAST_NAME = "last_name";
    public static final String COL_STATUS = "status";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";

    //======= Employee column definition ========
    public static final String EMAIL_DEFINITION = "VARCHAR(100)";
    public static final String PHONE_DEFINITION = "VARCHAR(20)";
    public static final String NAME_DEFINITION = "VARCHAR(100)";

    //======= Employee validation values limit ========
    public static final int MAX_CHARS_EMAIL = 100;
    public static final int MAX_CHARS_PHONE = 20;
    public static final int MAX_CHARS_NAME = 100;
}
