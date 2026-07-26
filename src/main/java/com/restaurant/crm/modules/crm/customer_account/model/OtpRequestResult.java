package com.restaurant.crm.modules.crm.customer_account.model;

import java.time.Instant;

/**
 * Outcome of an OTP request (uc-c-03), returned by the QR-agnostic {@code CustomerOtpService}.
 * Never carries the OTP code. The web layer (in erp/order) maps this to the wire response.
 */
public record OtpRequestResult(
        String maskedPhone,
        Instant expiresAt,
        Instant resendAvailableAt
) {
}
