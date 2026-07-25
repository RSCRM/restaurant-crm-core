package com.restaurant.crm.modules.erp.qr_ordering.model;

import com.restaurant.crm.modules.erp.qr_ordering.enums.QrSessionStatus;

import java.time.Instant;

/**
 * Typed view of the {@code qr:session:{sessionId}} Redis hash (uc-c-02).
 * {@code orderId} starts null and is filled by uc-c-05 via {@code bindOrder}.
 * {@code ownerCustomerId} may stay null in uc-c-02 (customer resolution is not
 * done here); the owner's phone is enough to ref the order at create time.
 */
public record QrSessionData(
        String sessionId,
        String organizationId,
        String branchId,
        String tableId,
        String ownerDeviceId,
        String ownerCustomerId,
        String ownerCustomerPhone,
        String orderId,
        QrSessionStatus status,
        Instant createdAt
) {
}
