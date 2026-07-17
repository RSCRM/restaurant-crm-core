package com.restaurant.crm.modules.identity.constants.user;

public class UserConstants {
    private UserConstants() {}

    //======= User table name ========
    public static final String TABLE_USER = "users";

    //======= User column name ========
    public static final String COL_USERNAME = "username";
    public static final String COL_PASSWORD = "password";
    public static final String COL_EMAIL = "email";
    public static final String COL_STATUS = "status";
    public static final String COL_ENABLED = "enabled";

    //======= User column definition ========
    public static final String USERNAME_DEFINITION = "VARCHAR(100)";
    public static final String PASSWORD_DEFINITION = "VARCHAR(100)";
    public static final String EMAIL_DEFINITION = "VARCHAR(255)";

    //======= User validation values limit ========
    public static final int MIN_CHARS_USERNAME = 5;
    public static final int MAX_CHARS_USERNAME = 50;
    public static final int MIN_CHARS_PASSWORD = 8;
    public static final int MAX_CHARS_PASSWORD = 100;
}
