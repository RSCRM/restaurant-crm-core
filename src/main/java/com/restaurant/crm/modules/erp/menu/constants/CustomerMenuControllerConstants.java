package com.restaurant.crm.modules.erp.menu.constants;

/**
 * Route constants for the customer menu endpoints (uc-c-04).
 * Not public — requires a CUSTOMER_SESSION token.
 */
public final class CustomerMenuControllerConstants {

    public static final String BASE = "/api/v1/customer/menu";
    public static final String PATH_PRODUCT = "/products/{productId}";

    /** Spring Security role required to browse the menu (granted to CUSTOMER_SESSION tokens). */
    public static final String ROLE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    private CustomerMenuControllerConstants() {}
}
