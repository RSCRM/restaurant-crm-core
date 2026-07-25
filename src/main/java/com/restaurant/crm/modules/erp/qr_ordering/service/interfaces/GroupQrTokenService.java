package com.restaurant.crm.modules.erp.qr_ordering.service.interfaces;

import com.restaurant.crm.modules.erp.qr_ordering.model.GroupQrPayload;

/**
 * Signs and verifies the dynamic GROUP QR token bound to an ordering session (uc-c-02).
 * Unlike the TABLE QR, the GROUP QR is a bearer capability: it always carries a short
 * {@code exp}, and callers must additionally re-check the session against Redis.
 * "Refresh" is not a crypto operation — {@code QrSessionService} re-issues a token via
 * {@link #generate} with a new TTL.
 */
public interface GroupQrTokenService {

    /**
     * Signs a GROUP QR token for a session (uc-c-02).
     *
     * @param payload    organization, branch, table and session to embed
     * @param ttlSeconds token lifetime in seconds (capped by the caller to the session TTL)
     * @return serialized compact JWS string
     */
    String generate(GroupQrPayload payload, long ttlSeconds);

    /**
     * Verifies a GROUP QR token's signature and expiry, returning its trusted payload (uc-c-02).
     * Does NOT check the session in Redis — the caller must still validate liveness and that
     * the embedded branch/table match the session hash (anti-splice, NFR-07).
     * Throws {@code AppException} with a {@code TQR_*} code on failure.
     *
     * @param token serialized compact JWS string read from the scanned QR
     * @return verified {@link GroupQrPayload}
     */
    GroupQrPayload verify(String token);
}
