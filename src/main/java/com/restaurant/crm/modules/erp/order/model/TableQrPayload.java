package com.restaurant.crm.modules.erp.order.model;


public record TableQrPayload(
        String organizationId,
        String branchId,
        String tableId,
        Integer qrVersion
) {
}
