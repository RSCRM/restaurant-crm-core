package com.restaurant.crm.common.enums;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.experimental.FieldDefaults;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public enum ErrorCode {
    SERVER_UNCATEGORIZED_EXCEPTION("SERVER_9999", "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== AUTHENTICATION ERRORS ====
    AUTH_UNAUTHENTICATED("AUTH_1000", "Unauthenticated", HttpStatus.UNAUTHORIZED),
    AUTH_MISSING_TOKEN("AUTH_1001", "Client missing token", HttpStatus.BAD_REQUEST),
    AUTH_GENERATION_FAIL("AUTH_1002", "Generation JWT fail", HttpStatus.INTERNAL_SERVER_ERROR),
    JWT_CLAIM_MISSING("AUTH_1003", "JWT claim is missing or invalid", HttpStatus.UNAUTHORIZED),
    EMAIL_ALREADY_EXISTS("AUTH_1005", "Email already exists", HttpStatus.BAD_REQUEST),
    AUTH_INVALID_TOKEN_TYPE("AUTH_1006", "Invalid token type", HttpStatus.UNAUTHORIZED),
    AUTH_CONTEXT_NOT_FOUND("AUTH_1007", "Context not found for user", HttpStatus.NOT_FOUND),
    AUTH_TOKEN_REVOKED("AUTH_1008", "Access token has been revoked", HttpStatus.UNAUTHORIZED),

    // ==== USER ERRORS ====
    USER_USERNAME_NOT_FOUND("USER_1000", "User not found with given username", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_1004", "User not found ", HttpStatus.NOT_FOUND),
    USER_USERNAME_ALREADY_EXISTS("USER_1001", "Username already exists", HttpStatus.BAD_REQUEST),
    USER_USERNAME_EXISTED("USER_1001", "Username already exists", HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED("USER_1002", "User email already verified", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS("USER_1003", "User already exists", HttpStatus.BAD_REQUEST),
    USER_EMAIL_INVALID("USER_1005", "Email is invalid", HttpStatus.BAD_REQUEST),
    USER_FULL_NAME_INVALID("USER_1006", "Full name is invalid", HttpStatus.BAD_REQUEST),
    USER_PHONE_INVALID("USER_1007", "Phone number is invalid", HttpStatus.BAD_REQUEST),
    USER_PHONE_ALREADY_EXISTS("USER_1008", "Phone number already exists", HttpStatus.BAD_REQUEST),

    // ==== ROLE ERRORS ====
    ROLE_NOT_FOUND("ROLE_1000", "Role not found", HttpStatus.NOT_FOUND),
    ROLE_NAME_EXISTED("ROLE_1001", "Role name existed", HttpStatus.BAD_REQUEST),

    // ==== PERMISSION ERRORS ====
    PERMISSION_NOT_FOUND("PERM_1000", "Permission not found", HttpStatus.NOT_FOUND),

    // ==== AUTHORIZATION ERRORS ====
    AUTHZ_UNAUTHORIZED("AUTHZ_1000", "You do not have permission", HttpStatus.FORBIDDEN),

    // ==== FILE UPLOAD ERRORS ====
    FILE_UPLOAD_FAILED("FILE_1000", "File upload failed", HttpStatus.INTERNAL_SERVER_ERROR),
    FILE_INVALID_TYPE("FILE_1001", "Invalid file type", HttpStatus.BAD_REQUEST),
    FILE_SIZE_EXCEEDED("FILE_1002", "File size exceeds limit", HttpStatus.BAD_REQUEST),
    FILE_EMPTY("FILE_1003", "File is empty", HttpStatus.BAD_REQUEST),
    FILE_INVALID_UPLOAD_TYPE("FILE_1004", "Invalid upload type", HttpStatus.BAD_REQUEST),
    FILE_REF_ID_REQUIRED("FILE_1005", "Reference id is required", HttpStatus.BAD_REQUEST),

    // ==== EMPLOYEE ERRORS ====
    EMPLOYEE_NOT_FOUND("EMP_1000", "Employee not found", HttpStatus.NOT_FOUND),
    EMPLOYEE_CODE_EXISTS("EMP_1001", "Employee code already exists", HttpStatus.BAD_REQUEST),
    EMPLOYEE_EMAIL_INVALID("EMP_1002", "Email not valid", HttpStatus.BAD_REQUEST),
    EMPLOYEE_NOT_ACTIVE("EMP_1003", "Employee is not active", HttpStatus.BAD_REQUEST),
    EMPLOYEE_ORG_ROLE_NOT_FOUND("EMP_1004", "Org role not found", HttpStatus.NOT_FOUND),
    EMPLOYEE_SALARY_INVALID("EMP_1005", "Salary must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    EMPLOYEE_USERNAME_REQUIRED("EMP_1006", "Username is required", HttpStatus.BAD_REQUEST),
    EMPLOYEE_EMAIL_REQUIRED("EMP_1007", "Email is required", HttpStatus.BAD_REQUEST),
    EMPLOYEE_PHONE_REQUIRED("EMP_1008", "Phone is required", HttpStatus.BAD_REQUEST),
    EMPLOYEE_BRANCH_REQUIRED("EMP_1009", "Branch id is required", HttpStatus.BAD_REQUEST),
    EMPLOYEE_START_DATE_REQUIRED("EMP_1010", "Start date is required", HttpStatus.BAD_REQUEST),
    EMPLOYEE_ORG_ROLE_REQUIRED("EMP_1011", "Org role id is required", HttpStatus.BAD_REQUEST),

    // ==== ORG ROLE ERRORS ====
    ORG_ROLE_NOT_FOUND("ORGROLE_1000", "Org role not found", HttpStatus.NOT_FOUND),
    ORG_ROLE_NAME_EXISTS("ORGROLE_1001", "Role name already exists in this organization", HttpStatus.BAD_REQUEST),
    ORG_ROLE_NAME_REQUIRED("ORGROLE_1002", "Role name is required", HttpStatus.BAD_REQUEST),
    ORG_ROLE_ORGANIZATION_REQUIRED("ORGROLE_1003", "Organization id is required", HttpStatus.BAD_REQUEST),
    ORG_ROLE_DATA_SCOPE_REQUIRED("ORGROLE_1004", "Data scope is required", HttpStatus.BAD_REQUEST),
    ORG_PERMISSION_NOT_FOUND("ORGROLE_1005", "Org permission not found", HttpStatus.NOT_FOUND),

    // ==== ATTENDANCE ERRORS ====
    ATTENDANCE_SHIFT_NOT_FOUND("ATT_1000", "No active shift found", HttpStatus.NOT_FOUND),
    ATTENDANCE_ALREADY_CHECKED_IN("ATT_1001", "Shift already checked in", HttpStatus.CONFLICT),
    ATTENDANCE_OPEN_RECORD_NOT_FOUND("ATT_1002", "No open attendance found", HttpStatus.NOT_FOUND),
    ATTENDANCE_DATE_RANGE_INVALID("ATT_1003", "Attendance date range is invalid", HttpStatus.BAD_REQUEST),
    ATTENDANCE_QR_INVALID("ATT_1004", "Attendance QR is invalid", HttpStatus.BAD_REQUEST),
    ATTENDANCE_QR_EXPIRED("ATT_1005", "Attendance QR has expired", HttpStatus.BAD_REQUEST),
    ATTENDANCE_QR_CONTEXT_MISMATCH("ATT_1006", "Attendance QR does not match shift context", HttpStatus.FORBIDDEN),
    ATTENDANCE_QR_GENERATION_FAILED("ATT_1007", "Attendance QR generation failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== SCHEDULE ERRORS ====
    SCHEDULE_DATE_RANGE_INVALID("SCHEDULE_1000", "Schedule date range is invalid", HttpStatus.BAD_REQUEST),
    SCHEDULE_DATE_RANGE_EXCEEDED(
            "SCHEDULE_1001",
            "Schedule date range cannot exceed 31 days",
            HttpStatus.BAD_REQUEST
    ),
    SCHEDULE_NOT_FOUND("SCHEDULE_1002", "Schedule not found", HttpStatus.NOT_FOUND),
    SCHEDULE_TIME_RANGE_INVALID("SCHEDULE_1003", "Schedule start time must be before end time", HttpStatus.BAD_REQUEST),
    SCHEDULE_CONFLICT("SCHEDULE_1004", "Employee already has a schedule at this start time", HttpStatus.CONFLICT),

    // ==== ORGANIZATION ERRORS ====
    ORGANIZATION_NOT_FOUND("ORG_1000", "Organization not found", HttpStatus.NOT_FOUND),
    ORGANIZATION_EXISTS("ORG_1001", "Organization already exists", HttpStatus.BAD_REQUEST),
    ORGANIZATION_TAX_CODE_EXISTS("ORG_1002", "Tax code already exists", HttpStatus.BAD_REQUEST),
    ORGANIZATION_INACTIVE("ORG_1003", "Organization is inactive", HttpStatus.BAD_REQUEST),

    // ==== ORGANIZATION BRANCH ERRORS ====
    ORGANIZATION_BRANCH_NOT_FOUND("BRANCH_1000", "Organization branch not found", HttpStatus.NOT_FOUND),
    ORGANIZATION_BRANCH_MANAGER_EXISTS("BRANCH_1001", "Manager is already assigned to another branch", HttpStatus.BAD_REQUEST),
    ORGANIZATION_BRANCH_INACTIVE("BRANCH_1002", "Organization branch is inactive", HttpStatus.BAD_REQUEST),
    ORGANIZATION_BRANCH_EXISTS("BRANCH_1003", "Organization branch already exists", HttpStatus.BAD_REQUEST),
    BRANCH_NOT_FOUND("BRANCH_1004", "Branch not found", HttpStatus.NOT_FOUND),
    BRANCH_MANAGER_NOT_FOUND("BRANCH_MANAGER_1000", "Branch manager not found", HttpStatus.NOT_FOUND),
    BRANCH_MANAGER_ALREADY_ASSIGNED("BRANCH_MANAGER_1001", "Branch manager is already assigned to another branch", HttpStatus.BAD_REQUEST),
    BRANCH_MANAGER_INACTIVE("BRANCH_MANAGER_1002", "Branch manager is inactive", HttpStatus.BAD_REQUEST),

    // ==== PROJECT ERRORS ====
    PROJECT_NOT_FOUND("PROJ_1000", "Project not found", HttpStatus.NOT_FOUND),
    PROJECT_CODE_EXISTS("PROJ_1001", "Project code already exists", HttpStatus.BAD_REQUEST),

    // ==== ALLOCATION ERRORS ====
    ALLOCATION_NOT_FOUND("ALLOC_1000", "Allocation not found", HttpStatus.NOT_FOUND),
    ALLOCATION_EXCEEDED("ALLOC_1001", "Employee allocation exceeds 100%", HttpStatus.BAD_REQUEST),
    ALLOCATION_PROJECT_COMPLETED("ALLOC_1002", "Cannot allocate to a completed project", HttpStatus.BAD_REQUEST),
    ALLOCATION_INVALID_PERCENT("ALLOC_1003", "Allocation percent must be between 1 and 100", HttpStatus.BAD_REQUEST),
    // ==== BOOKING ERRORS ====
    BOOKING_NOT_FOUND("BOOKING_1000", "Booking not found", HttpStatus.NOT_FOUND),

    // ==== TABLE ERRORS ====
    TABLE_AREA_NOT_FOUND("TABLE_1000", "Table area not found in branch", HttpStatus.NOT_FOUND),
    TABLE_SEARCH_CRITERIA_INVALID("TABLE_1001", "Table search criteria are invalid", HttpStatus.BAD_REQUEST),

    // ==== MENU ERRORS ====
    CATEGORY_NOT_FOUND("MENU_1000", "Category not found", HttpStatus.NOT_FOUND),
    CATEGORY_NAME_EXISTS("MENU_1001", "Category name already exists in this branch", HttpStatus.BAD_REQUEST),
    CATEGORY_NAME_REQUIRED("MENU_1002", "Category name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_NOT_FOUND("MENU_1010", "Product not found", HttpStatus.NOT_FOUND),
    PRODUCT_NAME_EXISTS("MENU_1011", "Product name already exists in this branch", HttpStatus.BAD_REQUEST),
    PRODUCT_NAME_REQUIRED("MENU_1012", "Product name is required", HttpStatus.BAD_REQUEST),
    PRODUCT_BRANCH_REQUIRED("MENU_1013", "Branch id is required", HttpStatus.BAD_REQUEST),
    MENU_CATEGORY_BRANCH_MISMATCH("MENU_1014", "Category does not belong to this branch", HttpStatus.BAD_REQUEST),
    MODIFIER_GROUP_NOT_FOUND("MENU_1020", "Modifier group not found", HttpStatus.NOT_FOUND),
    MODIFIER_GROUP_NAME_EXISTS("MENU_1021", "Group name already exists for this product", HttpStatus.BAD_REQUEST),
    MODIFIER_GROUP_NAME_REQUIRED("MENU_1022", "Group name is required", HttpStatus.BAD_REQUEST),
    MODIFIER_OPTION_NOT_FOUND("MENU_1023", "Modifier option not found", HttpStatus.NOT_FOUND),
    MODIFIER_OPTION_NAME_REQUIRED("MENU_1024", "Option name is required", HttpStatus.BAD_REQUEST),
    COMBO_NOT_FOUND("MENU_1030", "Combo not found", HttpStatus.NOT_FOUND),
    COMBO_NAME_EXISTS("MENU_1031", "Combo name already exists in this branch", HttpStatus.BAD_REQUEST),
    COMBO_NAME_REQUIRED("MENU_1032", "Combo name is required", HttpStatus.BAD_REQUEST),
    COMBO_BRANCH_REQUIRED("MENU_1033", "Branch id is required", HttpStatus.BAD_REQUEST),
    COMBO_ITEM_NOT_FOUND("MENU_1040", "Combo item not found", HttpStatus.NOT_FOUND),
    COMBO_ITEM_PRODUCT_EXISTS("MENU_1041", "Product already in this combo", HttpStatus.BAD_REQUEST),
    COMBO_ITEM_PRODUCT_BRANCH_MISMATCH("MENU_1042", "Product does not belong to the combo branch", HttpStatus.BAD_REQUEST),
    COMBO_ITEM_OPTIONS_MISMATCH("MENU_1043", "Must pick exactly one option per modifier group of the product", HttpStatus.BAD_REQUEST),
    COMBO_ITEM_PRODUCT_REQUIRED("MENU_1044", "Product id is required", HttpStatus.BAD_REQUEST),
    TABLE_AREA_NAME_EXISTS("TABLE_1002", "Area name already exists in this branch", HttpStatus.BAD_REQUEST),
    TABLE_AREA_BRANCH_REQUIRED("TABLE_1003", "Branch id is required", HttpStatus.BAD_REQUEST),
    TABLE_AREA_NAME_REQUIRED("TABLE_1004", "Area name is required", HttpStatus.BAD_REQUEST),
    RESTAURANT_TABLE_NOT_FOUND("TABLE_1005", "Restaurant table not found", HttpStatus.NOT_FOUND),
    RESTAURANT_TABLE_NUMBER_EXISTS("TABLE_1006", "Table number already exists in this area", HttpStatus.BAD_REQUEST),
    RESTAURANT_TABLE_AREA_REQUIRED("TABLE_1007", "Area id is required", HttpStatus.BAD_REQUEST),
    RESTAURANT_TABLE_NUMBER_REQUIRED("TABLE_1008", "Table number is required", HttpStatus.BAD_REQUEST),
    RESTAURANT_TABLE_CAPACITY_REQUIRED("TABLE_1009", "Capacity is required", HttpStatus.BAD_REQUEST),
    RESTAURANT_TABLE_CAPACITY_INVALID("TABLE_1010", "Capacity must be at least 1", HttpStatus.BAD_REQUEST),

    // ==== CUSTOMER ERRORS ====
    CUSTOMER_NOT_FOUND("CUST_1000", "Customer not found", HttpStatus.NOT_FOUND),
    CUSTOMER_PHONE_REQUIRED("CUST_1001", "Phone number is required", HttpStatus.BAD_REQUEST),
    CUSTOMER_PHONE_INVALID("CUST_1002", "Phone number must be between 9 and 15 digits", HttpStatus.BAD_REQUEST),

    // ==== ORDER ERRORS ====
    ORDER_BRANCH_ID_REQUIRED("ORDER_1000", "Branch id is required", HttpStatus.BAD_REQUEST),
    ORDER_TABLE_ID_REQUIRED("ORDER_1001", "Table id is required for dine-in order", HttpStatus.BAD_REQUEST),
    ORDER_BRANCH_NOT_FOUND("ORDER_1002", "Branch not found", HttpStatus.NOT_FOUND),
    ORDER_TABLE_NOT_FOUND("ORDER_1003", "Table not found in branch", HttpStatus.NOT_FOUND),
    ORDER_PRODUCT_NOT_FOUND("ORDER_1004", "Product not found in branch", HttpStatus.NOT_FOUND),
    ORDER_COMBO_NOT_FOUND("ORDER_1005", "Combo not found in branch", HttpStatus.NOT_FOUND),
    ORDER_MODIFIER_OPTION_NOT_FOUND("ORDER_1006", "Modifier option not found", HttpStatus.NOT_FOUND),
    ORDER_ITEM_NOT_FOUND("ORDER_1007", "Order item not found", HttpStatus.NOT_FOUND),
    ORDER_NOT_FOUND("ORDER_1008", "Order not found", HttpStatus.NOT_FOUND),
    ORDER_ITEM_STATUS_NOT_MODIFIABLE("ORDER_1009", "Order item status does not allow this modification", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_MODIFIER_NOT_FOUND("ORDER_1010", "Order item modifier not found", HttpStatus.NOT_FOUND),
    ORDER_STATUS_NOT_MODIFIABLE("ORDER_1011", "Only pending orders can be modified by staff", HttpStatus.BAD_REQUEST),
    ORDER_ALREADY_PAID("ORDER_1012", "Order has already been paid", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_ALREADY_ACCEPTED("ORDER_1013", "Order item has already been accepted by another chef", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_INVALID_STATUS_TRANSITION("ORDER_1014", "Invalid status transition for order item", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_NOT_PREPARED_BY_YOU("ORDER_1015", "You are not the chef who accepted this order item", HttpStatus.BAD_REQUEST),
    ORDER_ITEM_CANCEL_REASON_REQUIRED("ORDER_1016", "Reason is required when cancelling order item", HttpStatus.BAD_REQUEST),
    INVOICE_NOT_FOUND("INVOICE_1000", "Invoice not found", HttpStatus.NOT_FOUND),

    // ==== TABLE OPERATION ERRORS ====
    TABLE_NOT_FOUND("TABLE_SESSION_1000", "Table not found in branch", HttpStatus.NOT_FOUND),
    TABLE_NOT_AVAILABLE("TABLE_SESSION_1001", "Table is not available", HttpStatus.CONFLICT),
    TABLE_SESSION_ACTIVE_EXISTS("TABLE_SESSION_1002", "Table already has an active session", HttpStatus.CONFLICT),
    TABLE_PARTY_SIZE_EXCEEDS_CAPACITY(
            "TABLE_SESSION_1003",
            "Party size exceeds table capacity",
            HttpStatus.BAD_REQUEST
    ),
    TABLE_SESSION_NOT_FOUND("TABLE_SESSION_1004", "Table session not found", HttpStatus.NOT_FOUND),
    TABLE_SESSION_NOT_ACTIVE("TABLE_SESSION_1005", "Table session is not active", HttpStatus.CONFLICT),
    TABLE_TRANSFER_SAME_TABLE(
            "TABLE_SESSION_1006",
            "Source and target table must be different",
            HttpStatus.BAD_REQUEST
    ),
    TABLE_SESSION_UNPAID_ORDER("TABLE_SESSION_1007", "Table session has an unpaid order", HttpStatus.CONFLICT),

    // ==== INGREDIENT ERRORS ====
    INGREDIENT_CATEGORY_NOT_FOUND("INGREDIENT_CATEGORY_1000", "Ingredient category not found", HttpStatus.NOT_FOUND),
    INGREDIENT_CATEGORY_EXISTS("INGREDIENT_CATEGORY_1001", "Ingredient category already exists in this branch", HttpStatus.CONFLICT),
    INGREDIENT_NOT_FOUND("INGREDIENT_1000", "Ingredient not found", HttpStatus.NOT_FOUND),
    INGREDIENT_EXISTS("INGREDIENT_1001", "Ingredient already exists in this branch", HttpStatus.CONFLICT),

    // ==== INVENTORY ERRORS ====
    INVENTORY_NOT_FOUND("INV_1000", "Inventory not found", HttpStatus.NOT_FOUND),
    INVENTORY_EXISTS("INV_1001", "Inventory already exists for this ingredient", HttpStatus.CONFLICT),
    INVENTORY_INVALID_QUANTITY("INV_1002", "Inventory quantity must be greater than or equal to zero", HttpStatus.BAD_REQUEST),
    INVENTORY_INSUFFICIENT_STOCK("INV_1003", "Insufficient inventory quantity", HttpStatus.BAD_REQUEST),
    INVENTORY_OUT_OF_STOCK("INV_1004", "Ingredient is out of stock", HttpStatus.BAD_REQUEST),
    INVALID_DATE_RANGE("INV_1005", "Invalid date", HttpStatus.BAD_REQUEST),
    // ==== INVENTORY TRANSACTION ERRORS ====
    INVENTORY_TRANSACTION_NOT_FOUND("INV_TX_1000", "Inventory transaction not found", HttpStatus.NOT_FOUND),
    INVENTORY_TRANSACTION_INVALID_QUANTITY("INV_TX_1001", "Transaction quantity must be greater than zero", HttpStatus.BAD_REQUEST),
    INVENTORY_TRANSACTION_INVALID_TYPE("INV_TX_1002", "Invalid inventory transaction type", HttpStatus.BAD_REQUEST),
    INVENTORY_TRANSACTION_FAILED("INV_TX_1003", "Inventory transaction failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== RESTAURANT ERRORS ====
    RESTAURANT_ID_REQUIRED("REST_1000", "Restaurant ID is required", HttpStatus.BAD_REQUEST),

    // ==== LOYALTY ERRORS ====
    CUSTOMER_POINT_NOT_FOUND("LOY_1000", "Customer point wallet not found", HttpStatus.NOT_FOUND),
    CUSTOMER_POINT_INSUFFICIENT("LOY_1001", "Insufficient customer points", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_FOUND("LOY_1002", "Voucher not found", HttpStatus.NOT_FOUND),
    VOUCHER_INACTIVE("LOY_1003", "Voucher is inactive", HttpStatus.BAD_REQUEST),
    CUSTOMER_VOUCHER_NOT_FOUND("LOY_1004", "Customer voucher not found", HttpStatus.NOT_FOUND),
    CUSTOMER_VOUCHER_ALREADY_USED("LOY_1005", "Voucher has already been used", HttpStatus.BAD_REQUEST),
    CUSTOMER_VOUCHER_MIN_BILL_NOT_MET("LOY_1006", "Minimum bill amount not met", HttpStatus.BAD_REQUEST),
    VOUCHER_TITLE_REQUIRED("LOY_1007", "Voucher title is required", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_REQUIRED("LOY_1008", "Voucher discount percent is required", HttpStatus.BAD_REQUEST),
    VOUCHER_DISCOUNT_INVALID("LOY_1009", "Voucher discount percent must be between 1 and 100", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_BILL_REQUIRED("LOY_1010", "Voucher minimum bill amount is required", HttpStatus.BAD_REQUEST),
    VOUCHER_MIN_BILL_INVALID("LOY_1011", "Voucher minimum bill amount cannot be negative", HttpStatus.BAD_REQUEST),
    VOUCHER_POINTS_REQUIRED("LOY_1012", "Voucher points required is required", HttpStatus.BAD_REQUEST),
    VOUCHER_POINTS_INVALID("LOY_1013", "Voucher points required cannot be negative", HttpStatus.BAD_REQUEST),
    VOUCHER_ACTIVE_REQUIRED("LOY_1014", "Voucher status is required", HttpStatus.BAD_REQUEST),
    CUSTOMER_VOUCHER_EXPIRED("LOY_1015", "Voucher has expired", HttpStatus.BAD_REQUEST),

    // ==== LICENSE ERRORS ====
    LICENSE_NOT_FOUND("LICENSE_1000", "License not found", HttpStatus.NOT_FOUND),
    LICENSE_CODE_DUPLICATED("LICENSE_1001", "License code already exists", HttpStatus.CONFLICT),
    LICENSE_CODE_IMMUTABLE("LICENSE_1002", "License code cannot be changed", HttpStatus.BAD_REQUEST),
    LICENSE_ALREADY_LOCKED("LICENSE_1003", "License is already locked", HttpStatus.CONFLICT),
    LICENSE_ALREADY_ACTIVE("LICENSE_1004", "License is already active", HttpStatus.CONFLICT),
    LICENSE_STATUS_INVALID("LICENSE_1005", "License status is invalid for this action", HttpStatus.CONFLICT),
    LICENSE_CODE_REQUIRED("LICENSE_1006", "License code is required", HttpStatus.BAD_REQUEST),
    LICENSE_NAME_REQUIRED("LICENSE_1007", "License name is required", HttpStatus.BAD_REQUEST),
    LICENSE_PRICE_REQUIRED("LICENSE_1008", "License price is required", HttpStatus.BAD_REQUEST),
    LICENSE_PRICE_INVALID("LICENSE_1009", "Price must be greater than or equal to 0", HttpStatus.BAD_REQUEST),
    LICENSE_BILLING_CYCLE_REQUIRED("LICENSE_1010", "Billing cycle is required", HttpStatus.BAD_REQUEST),
    LICENSE_MAX_BRANCH_REQUIRED("LICENSE_1011", "Max branch is required", HttpStatus.BAD_REQUEST),
    LICENSE_MAX_BRANCH_INVALID("LICENSE_1012", "Max branch must be -1 or greater", HttpStatus.BAD_REQUEST),
    LICENSE_MAX_EMPLOYEE_REQUIRED("LICENSE_1013", "Max employee is required", HttpStatus.BAD_REQUEST),
    LICENSE_MAX_EMPLOYEE_INVALID("LICENSE_1014", "Max employee must be -1 or greater", HttpStatus.BAD_REQUEST),
    LICENSE_STATUS_REQUIRED("LICENSE_1015", "License status is required", HttpStatus.BAD_REQUEST),

    // ==== SUBSCRIPTION ERRORS ====
    SUBSCRIPTION_NOT_FOUND("SUB_1000", "Subscription not found", HttpStatus.NOT_FOUND),
    SUBSCRIPTION_ALREADY_REVOKED("SUB_1001", "Subscription is already revoked", HttpStatus.CONFLICT),
    ACTIVE_SUBSCRIPTION_EXISTS("SUB_1002", "Organization already has an active subscription", HttpStatus.CONFLICT),
    LICENSE_LOCKED_CANNOT_ISSUE("SUB_1003", "License is locked, cannot issue new subscription", HttpStatus.CONFLICT),

    // ==== TABLE QR / CUSTOMER SESSION ERRORS ====
    TQR_TOKEN_INVALID("TQR_1000", "QR token is malformed or invalid", HttpStatus.BAD_REQUEST),
    TQR_TOKEN_SIGNATURE_MISMATCH("TQR_1001", "QR token signature does not match", HttpStatus.UNAUTHORIZED),
    TQR_TOKEN_CLAIM_MISSING("TQR_1002", "QR token is missing a required claim", HttpStatus.BAD_REQUEST),
    TQR_VERSION_OUTDATED("TQR_1003", "QR version is outdated, please rescan the printed QR", HttpStatus.CONFLICT),
    TQR_CONTEXT_MISMATCH("TQR_1004", "QR context does not match branch/table/session", HttpStatus.FORBIDDEN),
    TQR_TABLE_NOT_IN_BRANCH("TQR_1005", "Table does not belong to the branch", HttpStatus.NOT_FOUND),
    TQR_SESSION_NOT_FOUND("TQR_1006", "Ordering session not found or already closed", HttpStatus.NOT_FOUND),
    TQR_SESSION_EXPIRED("TQR_1007", "Ordering session has expired", HttpStatus.UNAUTHORIZED),
    TQR_SESSION_LOCKED_FOR_PAYMENT("TQR_1008", "Ordering session is locked for payment", HttpStatus.CONFLICT),
    TQR_SESSION_MEMBER_LIMIT("TQR_1009", "Ordering session member limit reached", HttpStatus.CONFLICT),
    TQR_GENERATION_FAILED("TQR_1010", "QR token generation failed", HttpStatus.INTERNAL_SERVER_ERROR),
    TQR_TABLE_SESSION_EXISTS("TQR_1011", "Table already has an active session, ask the owner for the group QR", HttpStatus.CONFLICT),
    TQR_GROUP_QR_EXPIRED("TQR_1012", "Group QR has expired, ask the owner for a new one", HttpStatus.UNAUTHORIZED),
    TQR_NOT_SESSION_OWNER("TQR_1013", "Only the session owner can perform this action", HttpStatus.FORBIDDEN),
    TQR_OTP_TICKET_INVALID("TQR_1014", "OTP ticket is invalid or expired", HttpStatus.UNAUTHORIZED),

    // ==== CUSTOMER OTP ERRORS ====
    OTP_INVALID("OTP_1000", "OTP code is invalid", HttpStatus.BAD_REQUEST),
    OTP_EXPIRED("OTP_1001", "OTP code has expired or does not exist", HttpStatus.GONE),
    OTP_MAX_ATTEMPTS("OTP_1002", "Too many wrong OTP attempts, phone temporarily locked", HttpStatus.TOO_MANY_REQUESTS),
    OTP_PHONE_LOCKED("OTP_1003", "Phone is temporarily locked, try again later", HttpStatus.TOO_MANY_REQUESTS),
    OTP_RESEND_TOO_SOON("OTP_1004", "Please wait before requesting another OTP", HttpStatus.TOO_MANY_REQUESTS),
    OTP_TABLE_RATE_LIMIT("OTP_1005", "Too many OTP requests for this table, try again later", HttpStatus.TOO_MANY_REQUESTS),
    OTP_SEND_FAILED("OTP_1006", "Failed to send OTP", HttpStatus.BAD_GATEWAY),
    OTP_CUSTOMER_LOCKED("OTP_1007", "Customer account is locked", HttpStatus.FORBIDDEN),
    OTP_CONTEXT_MISMATCH("OTP_1008", "OTP was requested for a different table", HttpStatus.FORBIDDEN),
    OTP_TICKET_GENERATION_FAILED("OTP_1009", "OTP ticket generation failed", HttpStatus.INTERNAL_SERVER_ERROR),

    // ==== GROUP CART ERRORS ====
    CART_EMPTY("CART_1000", "Cart is empty", HttpStatus.BAD_REQUEST),
    CART_ITEM_NOT_FOUND("CART_1001", "Cart item not found", HttpStatus.NOT_FOUND),
    CART_ITEM_UNAVAILABLE("CART_1002", "One or more items are no longer available", HttpStatus.CONFLICT),
    CART_ITEM_LOCKED("CART_1003", "Another guest is editing this item", HttpStatus.CONFLICT),
    CART_LOCK_NOT_HELD("CART_1004", "This device does not hold the item lock", HttpStatus.FORBIDDEN),
    CART_SUBMIT_IN_PROGRESS("CART_1005", "The order is already being submitted", HttpStatus.CONFLICT),
    CART_MENU_ITEM_NOT_IN_BRANCH("CART_1006", "Menu item does not belong to this branch", HttpStatus.NOT_FOUND),
    CART_MODIFIER_INVALID("CART_1007", "Modifier option is invalid for this item", HttpStatus.BAD_REQUEST),
    CART_MEMBER_NOT_FOUND("CART_1008", "Session member not found for this device", HttpStatus.FORBIDDEN),
    CART_ITEM_REQUEST_INVALID("CART_1009", "Cart item request is invalid", HttpStatus.BAD_REQUEST),
    // ==== CUSTOMER MENU ERRORS ====
    MENU_BRANCH_CONTEXT_MISSING("MENU_1000", "Branch context is missing from the session", HttpStatus.FORBIDDEN),
    MENU_PRODUCT_NOT_FOUND("MENU_1001", "Product not found in this branch", HttpStatus.NOT_FOUND),
    MENU_EMPTY("MENU_1002", "Menu is not configured for this branch", HttpStatus.NOT_FOUND),

    // ==== CUSTOMER ORDER TRACKING ERRORS ====
    TRACK_NO_ACTIVE_ORDER("TRACK_1000", "No active order for this session yet", HttpStatus.CONFLICT),
    TRACK_ORDER_NOT_FOUND("TRACK_1001", "The order linked to this session no longer exists", HttpStatus.NOT_FOUND),
    ;

    String code;
    String message;
    HttpStatusCode httpStatusCode;
}
