package com.restaurant.crm.modules.identity.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.request.ContextSelectionRequest;
import com.restaurant.crm.modules.identity.dto.request.IntrospectRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.dto.response.ContextSelectionResponse;
import com.restaurant.crm.modules.identity.dto.response.IntrospectResponse;
import com.restaurant.crm.modules.identity.service.interfaces.AuthenticationService;
import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.HttpServletRequest;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/login")
    ResponseEntity<ApiResponse<AuthenticationResponse>> login(
            @Valid @RequestBody AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(authenticationService.authenticate(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/context")
    ResponseEntity<ApiResponse<ContextSelectionResponse>> selectContext(
            @Valid @RequestBody ContextSelectionRequest request,
            jakarta.servlet.http.HttpServletRequest httpRequest) {
        String identityToken = extractToken(httpRequest);
        ApiResponse<ContextSelectionResponse> response = ApiResponse.<ContextSelectionResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(authenticationService.selectContext(request, identityToken))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    ResponseEntity<ApiResponse<IntrospectResponse>> introspect(
            @RequestBody IntrospectRequest request) throws ParseException {
        ApiResponse<IntrospectResponse> response = ApiResponse.<IntrospectResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(authenticationService.introspect(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    ResponseEntity<ApiResponse<Void>> logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            ApiResponse<Void> response = ApiResponse.<Void>builder()
                    .success(ApiConstant.FAILURE)
                    .build();
            return ResponseEntity.status(401).body(response);
        }
        String token = authHeader.substring(7);
        authenticationService.logout(token);
        ApiResponse<Void> response = ApiResponse.<Void>builder()
                .success(ApiConstant.SUCCESS)
                .data(null)
                .build();
        return ResponseEntity.ok(response);
    }

    private String extractToken(jakarta.servlet.http.HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
