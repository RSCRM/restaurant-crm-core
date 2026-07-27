package com.restaurant.crm.modules.crm.customer_account.utils;

import com.restaurant.crm.modules.crm.customer_account.constants.CustomerOtpConstants;

import java.util.regex.Pattern;

/**
 * Phone normalization / validation / masking for OTP identification (uc-c-03).
 * New {@code utils/} layer in {@code customer_account}.
 * TODO(uc-c-03): consider consolidating with {@code CustomerConstants} phone rules
 * (owned by VuongTH6) once both parties agree on a single source of truth.
 */
public final class PhoneNumberUtils {

    private static final Pattern VN_PHONE = Pattern.compile(CustomerOtpConstants.PHONE_REGEX_VN);

    private PhoneNumberUtils() {}

    /**
     * Normalizes a raw phone to canonical {@code 0xxxxxxxxx} form (uc-c-03):
     * strips spaces/dots/dashes and maps {@code +84…}/{@code 84…} to a leading {@code 0}.
     * Returns the stripped string as-is if no country-code prefix is present (may be invalid;
     * validate with {@link #isValid}).
     */
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

    /** True when {@code phone} (already normalized) matches the VN mobile format (uc-c-03). */
    public static boolean isValid(String phone) {
        return phone != null && VN_PHONE.matcher(phone).matches();
    }

    /**
     * Masks the middle digits for safe display/logging (uc-c-03): {@code 0987654321 → 0987***321}.
     * Never returns enough to reconstruct the number.
     */
    public static String mask(String phone) {
        if (phone == null || phone.length() < 7) {
            return "***";
        }
        return phone.substring(0, 4) + "***" + phone.substring(phone.length() - 3);
    }
}
