package com.restaurant.crm.modules.crm.customer_account.constants;

/**
 * Constants for phone-OTP identification (uc-c-03, BR-CST-ACC-02).
 * New file — does not touch VuongTH6's {@code CustomerConstants}.
 */
public final class CustomerOtpConstants {

    /** Number of digits in an OTP code. */
    public static final int OTP_LENGTH = 6;

    /** OTP code lifetime — 180s (BR-CST-ACC-02). */
    public static final long CODE_TTL_SECONDS = 180L;

    /** Phone lockout after too many wrong attempts — 900s / 15 min (BR-CST-ACC-02). */
    public static final long LOCK_TTL_SECONDS = 900L;

    /** Minimum gap between resend requests for one phone — 60s. */
    public static final long RESEND_TTL_SECONDS = 60L;

    /** Wrong attempts that invalidate the current code — 3 (BR-CST-ACC-02). */
    public static final int MAX_ATTEMPTS = 3;

    /** Max OTP requests per table per hour (rate limit is per table, not per IP). */
    public static final int TABLE_RATE_LIMIT = 20;

    /** TTL window of the per-table rate-limit counter — 3600s / 1h. */
    public static final long TABLE_RATE_TTL_SECONDS = 3600L;

    /** OTP ticket lifetime — 300s / 5 min. */
    public static final long TICKET_TTL_SECONDS = 300L;

    /**
     * Vietnamese mobile number format, applied AFTER normalization to {@code 0xxxxxxxxx}.
     */
    public static final String PHONE_REGEX_VN = "^0(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$";

    /** {@code type} claim value of the OTP ticket token. */
    public static final String TICKET_TOKEN_TYPE = "OTP_TICKET";

    /** HMAC secret scope prefix for the OTP ticket: {@code "OTP_TICKET:" + branchId}. */
    public static final String TICKET_SECRET_SCOPE = "OTP_TICKET:";

    private CustomerOtpConstants() {}
}
