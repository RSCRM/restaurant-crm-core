package com.restaurant.crm.modules.identity.service.interfaces;

import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.request.ContextSelectionRequest;
import com.restaurant.crm.modules.identity.dto.request.IntrospectRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.dto.response.ContextSelectionResponse;
import com.restaurant.crm.modules.identity.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {

    /**
     * Authenticate user by email/password and return Identity Token + available contexts.
     * @param request {AuthenticationRequest}
     * @return AuthenticationResponse
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * Select a context (org/branch/employee) and return a Context Token.
     * @param request {ContextSelectionRequest}
     * @param identityToken raw Identity Token from Authorization header
     * @return ContextSelectionResponse
     */
    ContextSelectionResponse selectContext(ContextSelectionRequest request, String identityToken);

    /**
     * Introspect a token (validates signature and expiration).
     * @param request {IntrospectRequest}
     * @return IntrospectResponse
     */
    IntrospectResponse introspect(IntrospectRequest request) throws ParseException;
}
