package com.restaurant.crm.modules.erp.invoice.constants;

public class InvoiceConstants {
    private InvoiceConstants() {}

    public static final String TABLE_INVOICE = "invoices";

    public static final String COL_ORDER_ID = "order_id";
    public static final String COL_INVOICE_CODE = "invoice_code";
    public static final String COL_SUBTOTAL = "subtotal";
    public static final String COL_DISCOUNT_AMOUNT = "discount_amount";
    public static final String COL_TOTAL_AMOUNT = "total_amount";
    public static final String COL_PAYMENT_METHOD = "payment_method";
    public static final String COL_STATUS = "status";
    public static final String COL_PAID_AT = "paid_at";
    public static final String COL_REFUNDED_AT = "refunded_at";
    public static final String COL_REFUND_REASON = "refund_reason";
    public static final String COL_NOTE = "note";

    public static final String UUID_DEFINITION = "VARCHAR(36)";
    public static final String INVOICE_CODE_DEFINITION = "VARCHAR(30)";
    public static final String ENUM_DEFINITION = "VARCHAR(20)";
    public static final String REFUND_REASON_DEFINITION = "VARCHAR(255)";
    public static final String NOTE_DEFINITION = "VARCHAR(255)";

    public static final String INVOICE_CODE_PREFIX = "INV-";
    public static final int INVOICE_CODE_RANDOM_LENGTH = 8;
    public static final int MAX_CHARS_INVOICE_CODE = 30;
    public static final int MAX_CHARS_REFUND_REASON = 255;
    public static final int MAX_CHARS_NOTE = 255;

    public static final int MONEY_PRECISION = 10;
    public static final int MONEY_SCALE = 2;
}
