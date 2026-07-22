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
    AUTH_TOKEN_REVOKED("AUTH_1006", "Access token has been revoked", HttpStatus.UNAUTHORIZED),

    // ==== USER ERRORS ====
    USER_USERNAME_NOT_FOUND("USER_1000", "User not found with given username", HttpStatus.NOT_FOUND),
    USER_NOT_FOUND("USER_1004", "User not found ", HttpStatus.NOT_FOUND),
    USER_USERNAME_ALREADY_EXISTS("USER_1001", "Username already exists", HttpStatus.BAD_REQUEST),
    USER_ALREADY_VERIFIED("USER_1002", "User email already verified", HttpStatus.BAD_REQUEST),
    USER_ALREADY_EXISTS("USER_1003", "User already exists", HttpStatus.BAD_REQUEST),

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
    ;

    String code;
    String message;
    HttpStatusCode httpStatusCode;
}
