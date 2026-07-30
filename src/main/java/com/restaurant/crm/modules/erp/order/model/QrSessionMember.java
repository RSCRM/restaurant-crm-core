package com.restaurant.crm.modules.erp.order.model;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;

import java.time.Instant;


public record QrSessionMember(
        String deviceId,
        SessionMemberRole role,
        String customerId,
        String customerPhone,
        Instant joinedAt,
        Instant lastSeenAt
) {
}
