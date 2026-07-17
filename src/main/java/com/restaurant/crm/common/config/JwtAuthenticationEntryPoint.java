package com.restaurant.crm.common.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.ErrorMessage;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.common.enums.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.jspecify.annotations.NonNull;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import java.io.IOException;

public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {
    @Override
    public void commence(@NonNull HttpServletRequest request,
                         HttpServletResponse response,
                         @NonNull AuthenticationException authException) throws IOException, ServletException {
        ErrorCode errorCode = ErrorCode.AUTHZ_UNAUTHORIZED;
        ApiResponse<?> genericResponse = ApiResponse.builder()
                .success(ApiConstant.FAILURE)
                .errorMessage(ErrorMessage.builder()
                        .errorCode(errorCode.getCode())
                        .message(errorCode.getMessage())
                        .build())
                .build();
        ObjectMapper objectMapper = new ObjectMapper();
        response.setStatus(errorCode.getHttpStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write(objectMapper.writeValueAsString(genericResponse));
        response.flushBuffer();
    }
}
