package com.restaurant.crm.modules.erp.qr_ordering.model;

/**
 * Verified content of a TABLE QR token (uc-c-02).
 * Produced only after the HMAC signature has been validated.
 */
public record TableQrPayload(
        String organizationId,
        String branchId,
        String tableId,
        Integer qrVersion
) {
}
