package com.restaurant.crm.common.exception;

import com.restaurant.crm.common.enums.ErrorCode;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void returnsDeclaredHttpStatusForHandledErrors() {
        assertEquals(
                ErrorCode.ATTENDANCE_SHIFT_NOT_FOUND.getHttpStatusCode(),
                handler.handlingAppException(
                        new AppException(ErrorCode.ATTENDANCE_SHIFT_NOT_FOUND)).getStatusCode());
        assertEquals(
                ErrorCode.AUTHZ_UNAUTHORIZED.getHttpStatusCode(),
                handler.handlingAccessDeniedException(
                        new AccessDeniedException("denied")).getStatusCode());
    }
}
