package com.restaurant.crm.modules.crm.customeraccount.model;

import java.time.Instant;


public record OtpRequestResult(
        String maskedPhone,
        Instant expiresAt,
        Instant resendAvailableAt
) {
}
