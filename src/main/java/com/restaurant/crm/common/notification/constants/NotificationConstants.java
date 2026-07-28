package com.restaurant.crm.common.notification.constants;

/**
 * Constants for the shared notification infrastructure (table, column and limit definitions).
 */
public class NotificationConstants {

    private NotificationConstants() {}

    //======= Table names ========
    public static final String TABLE_NOTIFICATION = "notifications";
    public static final String TABLE_NOTIFICATION_RECEIPT = "notification_receipts";

    //======= notifications column names ========
    public static final String COL_SCOPE = "scope";
    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_TARGET_KEY = "target_key";
    public static final String COL_GROUP_TYPE = "group_type";
    public static final String COL_RECIPIENT_ID = "recipient_id";
    public static final String COL_SENDER_ID = "sender_id";
    public static final String COL_SENDER_TYPE = "sender_type";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_TYPE = "type";
    public static final String COL_REQUIRED_PERMISSION = "required_permission";
    public static final String COL_PRIORITY = "priority";
    public static final String COL_PAYLOAD = "payload";
    public static final String COL_EXPIRES_AT = "expires_at";
    public static final String COL_DEDUPE_KEY = "dedupe_key";

    //======= notification_receipts column names ========
    public static final String COL_NOTIFICATION_ID = "notification_id";
    public static final String COL_STATUS = "status";
    public static final String COL_READ_AT = "read_at";

    //======= Column definitions ========
    public static final String PAYLOAD_DEFINITION = "TEXT";

    //======= Validation value limits ========
    public static final int MAX_CHARS_TITLE = 100;
    public static final int MAX_CHARS_CONTENT = 255;
    public static final int MAX_CHARS_ENUM = 30;
    public static final int MAX_CHARS_TYPE = 40;
    public static final int MAX_CHARS_KEY = 64;
    public static final int MAX_CHARS_DEDUPE_KEY = 128;

    /**
     * Sentinel keeping the authority collection non-empty: an empty {@code IN} list in JPQL is
     * either a runtime error or a silently wrong predicate depending on the dialect.
     * {@link com.restaurant.crm.common.notification.security.RecipientContext} substitutes it.
     */
    public static final String NO_PERMISSION_SENTINEL = "__NO_PERMISSION__";

    /** Upper bound of notifications a single "mark all as read" call acknowledges. */
    public static final int MARK_ALL_READ_LIMIT = 500;
}