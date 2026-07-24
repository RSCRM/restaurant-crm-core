package com.restaurant.crm.modules.erp.shared.constants.employee;

public class EmployeeConstants {
    private EmployeeConstants() {
    }

    public static final String TABLE_EMPLOYEE = "employees";

    public static final String COL_USER_ID = "user_id";
    public static final String COL_ORG_ROLE_ID = "org_role_id";
    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_EMAIL = "email";
    public static final String COL_PHONE = "phone";
    public static final String COL_START_DATE = "start_date";
    public static final String COL_END_DATE = "end_date";
    public static final String COL_STATUS = "status";

    public static final String FULL_NAME_DEFINITION = "VARCHAR(100)";
    public static final String EMAIL_DEFINITION = "VARCHAR(100)";
    public static final String PHONE_DEFINITION = "VARCHAR(20)";

    public static final int MIN_CHARS_FULL_NAME = 1;
    public static final int MAX_CHARS_FULL_NAME = 100;
    public static final int MAX_CHARS_EMAIL = 100;
    public static final int MAX_CHARS_PHONE = 20;
}
