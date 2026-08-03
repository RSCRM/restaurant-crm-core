package com.restaurant.crm.modules.crm.customeraccount.model;


public record OtpTicketPayload(
        String customerPhone,
        String branchId,
        String tableId
) {
}
