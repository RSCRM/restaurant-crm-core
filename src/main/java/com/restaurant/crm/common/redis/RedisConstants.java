package com.restaurant.crm.common.redis;

public class RedisConstants {
    public static final String BLACKLIST_PREFIX = "jwt:blacklist:";

    // ==== QR ordering session keys (uc-c-02) ====
    public static final String QR_TABLE_PREFIX = "qr:table:";
    public static final String QR_SESSION_PREFIX = "qr:session:";
    public static final String QR_SESSION_MEMBERS_SUFFIX = ":members";
    public static final String QR_ORDER_PREFIX = "qr:order:";

    // ==== Customer OTP keys (uc-c-03) ====
    public static final String OTP_CODE_PREFIX = "otp:code:";
    public static final String OTP_LOCK_PREFIX = "otp:lock:";
    public static final String OTP_RESEND_PREFIX = "otp:resend:";
    public static final String OTP_TABLE_PREFIX = "otp:table:";

    private RedisConstants() {}
}
