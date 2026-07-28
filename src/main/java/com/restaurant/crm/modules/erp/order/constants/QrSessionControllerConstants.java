package com.restaurant.crm.modules.erp.order.constants;

/**
 * Route constants for the QR ordering controller (uc-c-02).
 * Public routes (no token) live under {@code /public/...}; authenticated
 * customer-session routes require a CUSTOMER_SESSION token.
 */
public final class QrSessionControllerConstants {

    public static final String BASE_PUBLIC = "/api/v1/public/customer/qr";
    public static final String BASE_SESSION = "/api/v1/customer/qr";

    public static final String PATH_RESOLVE = "/resolve";
    public static final String PATH_SESSION = "/session";
    public static final String PATH_SESSION_JOIN = "/session/join";
    public static final String PATH_SESSION_REFRESH_GROUP_QR = "/session/group-qr/refresh";
    public static final String PATH_SESSION_HEARTBEAT = "/session/heartbeat";

    /** Spring Security role granted to CUSTOMER_SESSION tokens (authority = ROLE_CUSTOMER_SESSION). */
    public static final String ROLE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    private QrSessionControllerConstants() {}
}
