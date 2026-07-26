package com.restaurant.crm.modules.erp.order.dto.response;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

/**
 * Session view returned to a participant (uc-c-02).
 * {@code groupQrToken}/{@code groupQrExpiresAt} are only populated for the OWNER —
 * members never receive the group QR (they cannot invite further people).
 * {@code orderId} is null until uc-c-05 creates the DB order.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@FieldDefaults(level = AccessLevel.PRIVATE)
public class QrSessionResponse {

    String sessionId;
    String deviceId;
    SessionMemberRole role;

    String sessionToken;
    Instant sessionExpiresAt;

    String groupQrToken;
    Instant groupQrExpiresAt;

    Integer memberCount;
    String orderId;

    String organizationId;
    String branchId;
    String tableId;
    String tableNumber;
}
