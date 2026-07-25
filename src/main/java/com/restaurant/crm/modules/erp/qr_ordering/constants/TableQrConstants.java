package com.restaurant.crm.modules.erp.qr_ordering.constants;

/**
 * Constants for the two QR token types used by uc-c-02.
 * Claim names live in {@code JwtClaimSetConstant}; this class only holds the
 * QR-specific token types and the HMAC secret scope prefixes.
 */
public final class TableQrConstants {

    /** {@code type} claim value of the static, printed-on-table QR. */
    public static final String TOKEN_TYPE_TABLE_QR = "TABLE_QR";

    /** {@code type} claim value of the dynamic, session-bound group QR. */
    public static final String TOKEN_TYPE_GROUP_QR = "GROUP_QR";

    /**
     * Secret scope prefix for TABLE QR: {@code "TABLE_QR:" + organizationId + ":" + branchId}.
     * Stable — never rotated by day, because the QR is printed on paper.
     */
    public static final String SECRET_SCOPE_TABLE_QR = "TABLE_QR:";

    /**
     * Secret scope prefix for GROUP QR: {@code "GROUP_QR:" + sessionId}.
     * Revoked implicitly once the session (and thus the sessionId) disappears from Redis.
     */
    public static final String SECRET_SCOPE_GROUP_QR = "GROUP_QR:";

    /** Default TABLE QR version when the printed QR predates the {@code qr_version} column. */
    public static final int DEFAULT_QR_VERSION = 1;

    private TableQrConstants() {}
}
