package com.restaurant.crm.modules.crm.customeraccount.model;

import java.time.Instant;


public record OtpCodeEntry(
        String codeHmac,
        int attempts,
        Instant issuedAt,
        String branchId,
        String tableId
) {
}
