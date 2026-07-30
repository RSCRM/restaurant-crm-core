package com.restaurant.crm.modules.erp.order.model;


public record GroupQrPayload(
        String organizationId,
        String branchId,
        String tableId,
        String sessionId
) {
}
