package com.restaurant.crm.modules.crm.customeraccount.model;

/**
 * Verified content of an OTP ticket (uc-c-03). New {@code model/} layer in customeraccount.
 * Produced only after the HMAC signature, expiry and {@code type} have been validated.
 */
public record OtpTicketPayload(
        String customerPhone,
        String branchId,
        String tableId
) {
}
