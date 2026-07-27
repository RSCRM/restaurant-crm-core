package com.restaurant.crm.modules.erp.order.model;

/**
 * Verified content of a GROUP QR token (uc-c-02).
 * Produced only after the HMAC signature and expiry have been validated.
 * The {@code branchId}/{@code tableId} are re-checked against the session hash
 * to prevent token splicing (mixing a GROUP QR with a foreign table/branch).
 */
public record GroupQrPayload(
        String organizationId,
        String branchId,
        String tableId,
        String sessionId
) {
}
