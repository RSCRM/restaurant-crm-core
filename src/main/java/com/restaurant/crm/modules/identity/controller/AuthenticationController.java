package com.restaurant.crm.modules.identity.controller;

import com.restaurant.crm.common.constant.ApiConstant;
import com.restaurant.crm.common.dto.response.ApiResponse;
import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.request.IntrospectRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.dto.response.IntrospectResponse;
import com.restaurant.crm.modules.identity.service.interfaces.AuthenticationService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.text.ParseException;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    AuthenticationService authenticationService;

    @PostMapping("/token")
    ResponseEntity<ApiResponse<AuthenticationResponse>> authenticate(@RequestBody AuthenticationRequest request) {
        ApiResponse<AuthenticationResponse> response = ApiResponse.<AuthenticationResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(authenticationService.authenticate(request))
                .build();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/introspect")
    ResponseEntity<ApiResponse<IntrospectResponse>> introspect(@RequestBody IntrospectRequest request) throws ParseException {
        ApiResponse<IntrospectResponse> response = ApiResponse.<IntrospectResponse>builder()
                .success(ApiConstant.SUCCESS)
                .data(authenticationService.introspect(request))
                .build();
        return ResponseEntity.ok(response);
    }

}
