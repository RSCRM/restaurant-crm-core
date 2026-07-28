package com.restaurant.crm.modules.erp.notification.constants;

/**
 * Routing constants for the staff-facing notification endpoints.
 */
public class NotificationControllerConstants {

    private NotificationControllerConstants() {}

    public static final String BASE_PATH = "/api/v1/notifications";

    public static final String PATH_SUBSCRIBE = "/subscribe";
    public static final String PATH_UNREAD_COUNT = "/unread-count";
    public static final String PATH_MARK_READ = "/{notificationId}/read";
    public static final String PATH_MARK_ALL_READ = "/read-all";

    public static final String PARAM_PAGE = "page";
    public static final String PARAM_SIZE = "size";
    public static final String DEFAULT_PAGE = "1";
    public static final String DEFAULT_SIZE = "10";
}