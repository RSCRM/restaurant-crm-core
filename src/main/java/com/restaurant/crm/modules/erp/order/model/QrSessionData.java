package com.restaurant.crm.modules.erp.order.model;

import com.restaurant.crm.modules.erp.order.enums.QrSessionStatus;

import java.time.Instant;


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
