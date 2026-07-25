package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.enums.SessionMemberRole;

import java.time.Instant;

/**
 * Issues the third token type — {@code CUSTOMER_SESSION} (uc-c-02).
 * Signed with {@code security.jwt.signer-key} so the shared {@code jwtDecoder} can
 * validate it; carries the session/device/role so the customer endpoints can
 * authorize without re-reading Redis on every call.
 */
public interface CustomerSessionTokenService {

    /**
     * Signs a CUSTOMER_SESSION token for a participant (uc-c-02).
     *
     * @param ttlSeconds token lifetime, matching the remaining session TTL
     * @return the serialized token and its exact expiry instant
     */
    IssuedToken issue(String sessionId,
                      String deviceId,
                      String organizationId,
                      String branchId,
                      String tableId,
                      SessionMemberRole role,
                      long ttlSeconds);

    /** A signed customer-session token together with its server-authoritative expiry. */
    record IssuedToken(String token, Instant expiresAt) {
    }
}
