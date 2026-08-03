package com.restaurant.crm.modules.erp.notification.constants;

/**
 * Constants for the Notification module, including table and column mappings.
 */
public class NotificationConstants {
    private NotificationConstants() {}

    public static final String TABLE_NOTIFICATION = "notifications";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_RECIPIENT_ID = "recipient_id";
    public static final String COL_SENDER_ID = "sender_id";
    public static final String COL_TITLE = "title";
    public static final String COL_CONTENT = "content";
    public static final String COL_TYPE = "type";
    public static final String COL_STATUS = "status";

    public static final int MAX_CHARS_TITLE = 100;
    public static final int MAX_CHARS_CONTENT = 255;
    public static final int MAX_CHARS_TYPE = 30;
    public static final int MAX_CHARS_STATUS = 20;
}
