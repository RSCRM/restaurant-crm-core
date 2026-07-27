package com.restaurant.crm.modules.erp.order.model;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;

import java.time.Instant;

/**
 * One participant of a QR ordering session (uc-c-02), stored as JSON in the
 * {@code qr:session:{sessionId}:members} Redis hash keyed by {@code deviceId}.
 * {@code customerId}/{@code customerPhone} are only populated for the OWNER
 * (members do not identify themselves in the current design).
 */
public record QrSessionMember(
        String deviceId,
        SessionMemberRole role,
        String customerId,
        String customerPhone,
        Instant joinedAt,
        Instant lastSeenAt
) {
}
