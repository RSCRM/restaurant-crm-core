package com.restaurant.crm.modules.erp.schedule.constants;

public final class WorkScheduleConstants {

    public static final String TABLE_WORK_SCHEDULE = "work_schedules";
    public static final String COL_EMPLOYEE_ID = "employee_id";
    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_WORK_DATE = "work_date";
    public static final String COL_START_TIME = "start_time";
    public static final String COL_END_TIME = "end_time";
    public static final String COL_NOTE = "note";
    public static final int MAX_NOTE_LENGTH = 500;
    public static final long MAX_RANGE_DAYS = 31;

    private WorkScheduleConstants() {
    }
}

