package com.restaurant.crm.modules.erp.order.constants;


public final class GroupCartControllerConstants {

    public static final String BASE = "/api/v1/customer/cart";
    public static final String PATH_ITEMS = "/items";
    public static final String PATH_ITEM = "/items/{cartItemId}";
    public static final String PATH_ITEM_LOCK = "/items/{cartItemId}/lock";
    public static final String PATH_SUBMIT = "/submit";
    public static final String PATH_SUBSCRIBE = "/subscribe";

    public static final String ROLE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    private GroupCartControllerConstants() {}
}
