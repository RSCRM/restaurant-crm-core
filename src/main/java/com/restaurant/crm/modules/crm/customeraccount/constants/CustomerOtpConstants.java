package com.restaurant.crm.modules.crm.customeraccount.constants;


public final class CustomerOtpConstants {


    public static final int OTP_LENGTH = 6;


    public static final long CODE_TTL_SECONDS = 180L;


    public static final long LOCK_TTL_SECONDS = 900L;


    public static final long RESEND_TTL_SECONDS = 60L;


    public static final int MAX_ATTEMPTS = 3;


    public static final int TABLE_RATE_LIMIT = 20;


    public static final long TABLE_RATE_TTL_SECONDS = 3600L;


    public static final long TICKET_TTL_SECONDS = 300L;


    public static final String PHONE_REGEX_VN = "^0(3[2-9]|5[2689]|7[06-9]|8[1-9]|9[0-9])[0-9]{7}$";


    public static final String TICKET_TOKEN_TYPE = "OTP_TICKET";


    public static final String TICKET_SECRET_SCOPE = "OTP_TICKET:";

    private CustomerOtpConstants() {}
}
