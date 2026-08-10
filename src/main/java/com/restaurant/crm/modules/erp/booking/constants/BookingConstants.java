package com.restaurant.crm.modules.erp.booking.constants;

public class BookingConstants {
    private BookingConstants() {}

    public static final String TABLE_NAME = "bookings";

    public static final String COL_BRANCH_ID = "branch_id";
    public static final String COL_TABLE_ID = "table_id";
    public static final String COL_CUSTOMER_ID = "customer_id";
    public static final String COL_BOOKING_TIME = "booking_time";
    public static final String COL_GUEST_COUNT = "guest_count";
    public static final String COL_STATUS = "status";

    public static final int MIN_GUEST_COUNT = 1;
    public static final int MAX_NOTE_LENGTH = 255;

    public static final String COL_NOTE = "note";
    //error
    public static final String BOOKING_STATUS_REQUIRE = "BOOKING_STATUS_REQUIRE";
}
