package com.restaurant.crm.modules.erp.constants.license;

public class LicenseContractConstants {
    private LicenseContractConstants() {
    }

    public static final String TABLE_LICENSE_CONTRACT = "license_contracts";

    public static final String COL_ORGANIZATION_ID = "organization_id";
    public static final String COL_LICENSE_PLAN_ID = "license_plan_id";
    public static final String COL_LICENSE_KEY = "license_key";
    public static final String COL_STATUS = "status";
    public static final String COL_STARTED_AT = "started_at";
    public static final String COL_EXPIRED_AT = "expired_at";
    public static final String COL_REVOKED_AT = "revoked_at";
    public static final String COL_NOTE = "note";

    public static final String LICENSE_KEY_DEFINITION = "VARCHAR(120)";
    public static final String NOTE_DEFINITION = "VARCHAR(255)";
}
