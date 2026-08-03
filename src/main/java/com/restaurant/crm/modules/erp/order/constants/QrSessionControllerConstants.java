package com.restaurant.crm.modules.erp.order.constants;

public final class QrSessionControllerConstants {

    public static final String BASE_PUBLIC = "/api/v1/public/customer/qr";
    public static final String BASE_SESSION = "/api/v1/customer/qr";

    public static final String PATH_RESOLVE = "/resolve";
    public static final String PATH_SESSION = "/session";
    public static final String PATH_SESSION_JOIN = "/session/join";
    public static final String PATH_SESSION_REFRESH_GROUP_QR = "/session/group-qr/refresh";
    public static final String PATH_SESSION_HEARTBEAT = "/session/heartbeat";

    public static final String ROLE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    private QrSessionControllerConstants() {}
}
