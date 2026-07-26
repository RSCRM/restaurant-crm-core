package com.restaurant.crm.modules.crm.customer_account.model;

/**
 * Verified content of an OTP ticket (uc-c-03). New {@code model/} layer in customer_account.
 * Produced only after the HMAC signature, expiry and {@code type} have been validated.
 */
public record OtpTicketPayload(
        String customerPhone,
        String organizationId,
        String branchId,
        String tableId
) {
}
