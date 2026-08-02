package com.restaurant.crm.modules.crm.customeraccount.utils;

import java.util.regex.Pattern;

import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerOtpConstants;

public final class PhoneNumberUtils {

    private static final Pattern VN_PHONE = Pattern.compile(CustomerOtpConstants.PHONE_REGEX_VN);

    private PhoneNumberUtils() {}


    public static String normalize(String raw) {
        if (raw == null) {
            return null;
        }
        String digits = raw.replaceAll("[\\s.\\-]", "");
        if (digits.startsWith("+84")) {
            return "0" + digits.substring(3);
        }
        if (digits.startsWith("84")) {
            return "0" + digits.substring(2);
        }
        return digits;
    }


    public static boolean isValid(String phone) {
        return phone != null && VN_PHONE.matcher(phone).matches();
    }


    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }
}
