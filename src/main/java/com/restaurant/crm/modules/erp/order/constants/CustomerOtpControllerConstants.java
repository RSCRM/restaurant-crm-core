package com.restaurant.crm.modules.erp.order.constants;

/**
 * Route constants for the public OTP identification endpoints (uc-c-03).
 * Lives with the controller in erp/order (the composition layer that knows both QR and OTP).
 */
public final class CustomerOtpControllerConstants {

    public static final String BASE_PUBLIC = "/api/v1/public/customer/otp";
    public static final String PATH_REQUEST = "/request";
    public static final String PATH_VERIFY = "/verify";

    private CustomerOtpControllerConstants() {}
}
