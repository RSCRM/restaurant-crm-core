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

    // ==== PROJECT ERRORS ====
    PROJECT_NOT_FOUND("PROJ_1000", "Project not found", HttpStatus.NOT_FOUND),
    PROJECT_CODE_EXISTS("PROJ_1001", "Project code already exists", HttpStatus.BAD_REQUEST),

    // ==== ALLOCATION ERRORS ====
    ALLOCATION_NOT_FOUND("ALLOC_1000", "Allocation not found", HttpStatus.NOT_FOUND),
    ALLOCATION_EXCEEDED("ALLOC_1001", "Employee allocation exceeds 100%", HttpStatus.BAD_REQUEST),
    ALLOCATION_PROJECT_COMPLETED("ALLOC_1002", "Cannot allocate to a completed project", HttpStatus.BAD_REQUEST),
    ALLOCATION_INVALID_PERCENT("ALLOC_1003", "Allocation percent must be between 1 and 100", HttpStatus.BAD_REQUEST),

    ;

    String code;
    String message;
    HttpStatusCode httpStatusCode;
}
