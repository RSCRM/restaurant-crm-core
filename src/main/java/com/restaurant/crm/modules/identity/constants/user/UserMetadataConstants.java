package com.restaurant.crm.modules.identity.constants.user;

public final class UserMetadataConstants {

    // =========================
    // TABLE
    // =========================
    public static final String TABLE_USER_METADATA = "user_metadata";
    public static final String TABLE_USER_METADATA_CATEGORIES = "user_metadata_categories";

    // =========================
    // COLUMNS
    // =========================
    public static final String COL_USER_ID = "user_id";
    public static final String COL_USER_METADATA_ID = "user_metadata_id";
    public static final String COL_CURRENT_INTENT_TEXT = "current_intent_text";
    public static final String COL_EXPIRES_AT = "expires_at";
    public static final String COL_CATEGORY_ID = "category_id";

    // =========================
    // DEFINITIONS
    // =========================
    public static final String USER_ID_DEFINITION = "VARCHAR(36)";
    public static final String CURRENT_INTENT_TEXT_DEFINITION = "TEXT";
    public static final String EXPIRES_AT_DEFINITION = "TIMESTAMP";
    public static final String CATEGORY_ID_DEFINITION = "VARCHAR(36)";

    private UserMetadataConstants() {
        // prevent instantiation
    }
}

