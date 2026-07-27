package com.restaurant.crm.modules.crm.customeraccount.model;

import java.time.Instant;

/**
 * Typed view of the {@code otp:code:{phone}} Redis hash (uc-c-03).
 * The raw OTP code is never stored — only its phone-bound HMAC.
 * {@code branchId}/{@code tableId} let verify detect a customer switching tables
 * between request and verify ({@code OTP_CONTEXT_MISMATCH}).
 */
public record OtpCodeEntry(
        String codeHmac,
        int attempts,
        Instant issuedAt,
        String branchId,
        String tableId
) {
}
