package com.restaurant.crm.modules.identity.service.interfaces;


import com.restaurant.crm.modules.identity.dto.request.AuthenticationRequest;
import com.restaurant.crm.modules.identity.dto.request.IntrospectRequest;
import com.restaurant.crm.modules.identity.dto.response.AuthenticationResponse;
import com.restaurant.crm.modules.identity.dto.response.IntrospectResponse;

import java.text.ParseException;

public interface AuthenticationService {
    /**
     * authenticate
     * @author catsocute
     * @param request {AuthenticationRequest}
     * @return AuthenticationResponse
     */
    AuthenticationResponse authenticate(AuthenticationRequest request);

    /**
     * introspect token
     * @author catsocute
     * @param request {IntrospectRequest}
     * @return IntrospectResponse
     */
    IntrospectResponse introspect(IntrospectRequest request) throws ParseException;
}
