package com.restaurant.crm.modules.erp.qr_ordering.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.qr_ordering.enums.SessionMemberRole;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.CustomerSessionTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;

/**
 * Mints CUSTOMER_SESSION tokens (uc-c-02). Uses the shared JWT signer key so the
 * existing resource-server {@code jwtDecoder} accepts the token; the SecurityConfig
 * converter maps {@code type=CUSTOMER_SESSION} to authority {@code ROLE_CUSTOMER_SESSION}.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class CustomerSessionTokenServiceImpl implements CustomerSessionTokenService {

    private static final String TOKEN_TYPE_CUSTOMER_SESSION = "CUSTOMER_SESSION";

    @NonFinal
    @Value("${security.jwt.signer-key}")
    String signerKey;

    @Override
    public IssuedToken issue(String sessionId,
                             String deviceId,
                             String organizationId,
                             String branchId,
                             String tableId,
                             SessionMemberRole role,
                             long ttlSeconds) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(ttlSeconds);
        try {
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .subject(sessionId)
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim(JwtClaimSetConstant.CLAIM_TYPE, TOKEN_TYPE_CUSTOMER_SESSION)
                    .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, organizationId)
                    .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, branchId)
                    .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, tableId)
                    .claim(JwtClaimSetConstant.CLAIM_SESSION_ID, sessionId)
                    .claim(JwtClaimSetConstant.CLAIM_DEVICE_ID, deviceId)
                    .claim(JwtClaimSetConstant.CLAIM_SESSION_ROLE, role.name())
                    .build();
            SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            token.sign(new MACSigner(signerKey.getBytes(StandardCharsets.UTF_8)));
            return new IssuedToken(token.serialize(), expiresAt);
        } catch (JOSEException exception) {
            throw new AppException(ErrorCode.TQR_GENERATION_FAILED);
        }
    }
}
