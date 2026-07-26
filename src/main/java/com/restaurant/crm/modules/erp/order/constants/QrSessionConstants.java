package com.restaurant.crm.modules.erp.order.constants;

/**
 * Tunable limits for QR ordering sessions (uc-c-02).
 */
public final class QrSessionConstants {

    /** Redis TTL of a session and all of its keys — 4 hours. */
    public static final long SESSION_TTL_SECONDS = 14400L;

    /**
     * Default GROUP QR lifetime — 30 minutes.
     * Kept short because the GROUP QR is a bearer capability (it can be screenshotted
     * and forwarded off-premise); the owner may request a fresh one via group-qr/refresh.
     */
    public static final long GROUP_QR_TTL_SECONDS = 1800L;

    /** Maximum number of participants (owner + members) allowed in one session. */
    public static final int MAX_MEMBERS = 12;

    private QrSessionConstants() {}
}
