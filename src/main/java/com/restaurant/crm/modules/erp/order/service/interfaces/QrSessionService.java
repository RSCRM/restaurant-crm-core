package com.restaurant.crm.modules.erp.order.service.interfaces;

import com.restaurant.crm.modules.erp.order.dto.request.QrResolveRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionJoinRequest;
import com.restaurant.crm.modules.erp.order.dto.request.QrSessionStartRequest;
import com.restaurant.crm.modules.erp.order.dto.response.QrResolveResponse;
import com.restaurant.crm.modules.erp.order.dto.response.QrSessionResponse;

/**
 * Orchestrates QR table ordering sessions (uc-c-02): scan → resolve → open (OWNER) →
 * join (MEMBER) → keep alive. Sessions live in Redis and are independent of the DB order,
 * which is only created later (uc-c-05) and linked back via {@link #bindOrder}.
 */
public interface QrSessionService {

    /**
     * Verifies a scanned TABLE QR and returns table info without creating anything (uc-c-02).
     * Read-only: never writes to Redis (avoids ghost sessions from curious scans).
     */
    QrResolveResponse resolve(QrResolveRequest request);

    /**
     * Opens a new session as OWNER after OTP has passed (uc-c-02).
     * Owner election is atomic (SETNX on the table pointer); losers get {@code TQR_1011}.
     * Does not create a DB order ({@code orderId} stays null).
     */
    QrSessionResponse start(QrSessionStartRequest request);

    /**
     * Joins an existing session as MEMBER via a GROUP QR (uc-c-02).
     * Members never receive the group QR back and cannot invite further people.
     */
    QrSessionResponse join(QrSessionJoinRequest request);

    /** Returns the caller's current session, read from the CUSTOMER_SESSION token (uc-c-02). */
    QrSessionResponse getCurrent();

    /** OWNER-only: issues a fresh GROUP QR for the session (uc-c-02); members get {@code TQR_1013}. */
    QrSessionResponse refreshGroupQr();

    /** Updates the caller's {@code lastSeenAt} and extends the TTL of all four session keys (uc-c-02). */
    QrSessionResponse heartbeat();

    /**
     * Links a session to its DB order both ways (uc-c-02 contract for uc-c-05).
     * Idempotent; call this right after {@code OrderService.create()}.
     */
    void bindOrder(String sessionId, String orderId);
}
