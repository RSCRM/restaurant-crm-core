package com.restaurant.crm.modules.identity.constants.user;

public final class UserProfileConstants {
    public static final String TABLE_USER_PROFILE = "user_profiles";

    public static final String COL_USER_ID = "user_id";
    public static final String COL_FULL_NAME = "full_name";
    public static final String COL_PHONE = "phone";

    public static final String FULL_NAME_DEFINITION = "VARCHAR(255)";
    public static final String PHONE_DEFINITION = "VARCHAR(15)";

    public static final int MIN_CHARS_FULL_NAME = 2;
    public static final int MAX_CHARS_FULL_NAME = 255;
    public static final String PHONE_PATTERN = "^\\+?[0-9]{9,15}$";

    private UserProfileConstants() {}
}
