package com.restaurant.crm.modules.erp.attendance.constants;

public final class AttendanceConstants {
    public static final String TABLE_ATTENDANCE = "attendances";
    public static final String TABLE_SHIFT_ASSIGNMENT = "shift_assignments";
    public static final String COL_SHIFT_ASSIGNMENT_ID = "shift_assignment_id";
    public static final String COL_EMPLOYEE_ID = "employee_id";
    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_WORK_DATE = "work_date";
    public static final String COL_START_AT = "start_at";
    public static final String COL_END_AT = "end_at";
    public static final String COL_CHECK_IN_AT = "check_in_at";
    public static final String COL_CHECK_OUT_AT = "check_out_at";
    public static final String COL_STATUS = "status";
    public static final long LATE_THRESHOLD_MINUTES = 15;
    public static final long QR_VALIDITY_SECONDS = 60;
    public static final String QR_TOKEN_TYPE = "ATTENDANCE_QR";
    public static final String CLAIM_QR_SESSION_ID = "qrSessionId";
    public static final String CLAIM_NONCE = "nonce";

    private AttendanceConstants() {}
}
