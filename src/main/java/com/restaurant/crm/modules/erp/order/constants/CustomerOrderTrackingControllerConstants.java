package com.restaurant.crm.modules.erp.order.constants;

/**
 * Route constants for the customer order-tracking endpoints (uc-c-06).
 * Not public — every route requires a CUSTOMER_SESSION token.
 */
public final class CustomerOrderTrackingControllerConstants {

    public static final String BASE = "/api/v1/customer/orders/current";
    public static final String PATH_COOKING_STATUS = "/cooking-status";
    public static final String PATH_SUBSCRIBE = "/cooking-status/subscribe";

    /** Spring Security role required for every tracking endpoint (granted to CUSTOMER_SESSION tokens). */
    public static final String ROLE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    private CustomerOrderTrackingControllerConstants() {}
}
