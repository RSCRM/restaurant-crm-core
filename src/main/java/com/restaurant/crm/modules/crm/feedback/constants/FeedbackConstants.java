package com.restaurant.crm.modules.crm.feedback.constants;

public class FeedbackConstants {
    private FeedbackConstants() {}

    public static final String TABLE_FEEDBACK = "feedbacks";

    public static final String COL_CUSTOMER_ID = "customer_id";
    public static final String COL_RESTAURANT_ID = "restaurant_id";
    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_RATING_FOOD = "rating_food";
    public static final String COL_RATING_SERVICE = "rating_service";
    public static final String COL_COMMENT = "comment";

    public static final String COMMENT_DEFINITION = "TEXT";

    public static final int MIN_RATING = 1;
    public static final int MAX_RATING = 5;
}
