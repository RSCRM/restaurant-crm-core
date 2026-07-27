package com.restaurant.crm.common.constant;

public class JwtClaimSetConstant {
    public static final String CLAIM_SCOPE = "scope";
    public static final String CLAIM_PERMISSION = "permission";
    public static final String CLAIM_USER_ID = "userId";
    public static final String CLAIM_TYPE = "type";
    public static final String CLAIM_ORGANIZATION_ID = "organizationId";
    public static final String CLAIM_BRANCH_ID = "branchId";
    public static final String CLAIM_EMPLOYEE_ID = "employeeId";
    public static final String CLAIM_ORG_ROLE = "orgRole";

    // ==== QR ordering / customer session claims (uc-c-02) ====
    public static final String CLAIM_TABLE_ID = "tableId";
    public static final String CLAIM_SESSION_ID = "sessionId";
    public static final String CLAIM_DEVICE_ID = "deviceId";
    public static final String CLAIM_SESSION_ROLE = "sessionRole";
    public static final String CLAIM_QR_VERSION = "qrVersion";

    // ==== OTP ticket claims (uc-c-03) ====
    public static final String CLAIM_CUSTOMER_PHONE = "customerPhone";
    public static final String CLAIM_JTI = "jti";
}
