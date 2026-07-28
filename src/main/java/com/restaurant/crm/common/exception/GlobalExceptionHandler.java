package com.restaurant.crm.common.exception;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.ErrorMessage;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Objects;

@RestControllerAdvice
public class GlobalExceptionHandler extends RuntimeException{
    @ExceptionHandler(value = RuntimeException.class)
    public ResponseEntity<ApiResponse<Object>> handlingRuntimeException(RuntimeException exception){
        exception.printStackTrace(); // Log stack trace for debugging
        ApiResponse<Object> response = ApiResponse.builder()
                .success(ApiConstant.FAILURE)
                .errorMessage(ErrorMessage.builder()
                        .errorCode(ErrorCode.SERVER_UNCATEGORIZED_EXCEPTION.getCode())
                        .message(ErrorCode.SERVER_UNCATEGORIZED_EXCEPTION.getMessage())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }

    @ExceptionHandler(value = AppException.class)
    public ResponseEntity<ApiResponse<Object>> handlingAppException(AppException exception){
        ErrorCode errorCode = exception.getErrorCode();
        ApiResponse<Object> response = ApiResponse.builder()
                .success(ApiConstant.FAILURE)
                .errorMessage(ErrorMessage.builder()
                        .errorCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();
        return ResponseEntity.ok(response);
    }

    //handling Denied Access
    @ExceptionHandler(value = {
            AccessDeniedException.class,
            AuthorizationDeniedException.class
    })
    ResponseEntity<ApiResponse<Object>> handlingAccessDeniedException(RuntimeException exception) {
        ErrorCode errorCode = ErrorCode.AUTHZ_UNAUTHORIZED;
        ApiResponse<Object> response = ApiResponse.builder()
                .success(ApiConstant.FAILURE)
                .errorMessage(ErrorMessage.builder()
                        .errorCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();
        return ResponseEntity.status(errorCode.getHttpStatusCode()).body(response);
    }

    //handling MethodArgumentNotValidException
    @ExceptionHandler(value = MethodArgumentNotValidException.class)
    ResponseEntity<ApiResponse<Object>> handlingMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        String enumKey = Objects.requireNonNull(exception.getFieldError()).getDefaultMessage();
        ErrorCode errorCode = ErrorCode.valueOf(enumKey);
        ApiResponse<Object> response = ApiResponse.builder()
                .success(ApiConstant.FAILURE)
                .errorMessage(ErrorMessage.builder()
                        .errorCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();

        return ResponseEntity.ok(response);
    }
}
