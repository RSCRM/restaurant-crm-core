package com.restaurant.crm.modules.erp.table.constants;

public final class TableSessionConstants {

    public static final String TABLE_NAME = "table_sessions";
    public static final String ID_COLUMN = "table_session_id";
    public static final String BRANCH_ID_COLUMN = "branch_id";
    public static final String TABLE_ID_COLUMN = "table_id";
    public static final String GUEST_NAME_COLUMN = "guest_name";
    public static final String GUEST_PHONE_COLUMN = "guest_phone";
    public static final String PARTY_SIZE_COLUMN = "party_size";
    public static final String STATUS_COLUMN = "status";
    public static final String STARTED_AT_COLUMN = "started_at";
    public static final String ENDED_AT_COLUMN = "ended_at";
    public static final String NOTE_COLUMN = "note";
    public static final int MAX_GUEST_NAME_LENGTH = 100;
    public static final int MAX_GUEST_PHONE_LENGTH = 15;
    public static final int MAX_NOTE_LENGTH = 255;

    private TableSessionConstants() {
    }
}
