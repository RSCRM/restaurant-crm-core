package com.restaurant.crm.modules.erp.qr_ordering.service.impl;

import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.qr_ordering.constants.TableQrConstants;
import com.restaurant.crm.modules.erp.qr_ordering.model.GroupQrPayload;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.GroupQrTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.time.Instant;
import java.util.Date;

/**
 * GROUP QR crypto (uc-c-02). Secret is scoped to the {@code sessionId}, so the
 * token dies when the session (and its sessionId) leaves Redis. Always short-lived.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupQrTokenServiceImpl implements GroupQrTokenService {

    @NonFinal
    @Value("${security.qr.signer-key:${security.jwt.signer-key}}")
    String qrSignerKey;

    @Override
    public String generate(GroupQrPayload payload, long ttlSeconds) {
        Instant now = Instant.now();
        try {
            byte[] secret = deriveGroupSecret(payload.sessionId());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(now.plusSeconds(ttlSeconds)))
                    .claim(JwtClaimSetConstant.CLAIM_TYPE, TableQrConstants.TOKEN_TYPE_GROUP_QR)
                    .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, payload.organizationId())
                    .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, payload.branchId())
                    .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, payload.tableId())
                    .claim(JwtClaimSetConstant.CLAIM_SESSION_ID, payload.sessionId())
                    .build();
            SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            token.sign(new MACSigner(secret));
            return token.serialize();
        } catch (GeneralSecurityException | JOSEException exception) {
            throw new AppException(ErrorCode.TQR_GENERATION_FAILED);
        }
    }

    @Override
    public GroupQrPayload verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS512.equals(jwt.getHeader().getAlgorithm())) {
                throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!TableQrConstants.TOKEN_TYPE_GROUP_QR
                    .equals(claims.getStringClaim(JwtClaimSetConstant.CLAIM_TYPE))) {
                throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
            }

            String sessionId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_SESSION_ID);
            if (isBlank(sessionId)) {
                throw new AppException(ErrorCode.TQR_TOKEN_CLAIM_MISSING);
            }

            byte[] secret = deriveGroupSecret(sessionId);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new AppException(ErrorCode.TQR_TOKEN_SIGNATURE_MISMATCH);
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null) {
                throw new AppException(ErrorCode.TQR_TOKEN_CLAIM_MISSING);
            }
            if (!expiration.toInstant().isAfter(Instant.now())) {
                throw new AppException(ErrorCode.TQR_GROUP_QR_EXPIRED);
            }

            String organizationId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID);
            String branchId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID);
            String tableId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_TABLE_ID);
            if (isBlank(organizationId) || isBlank(branchId) || isBlank(tableId)) {
                throw new AppException(ErrorCode.TQR_TOKEN_CLAIM_MISSING);
            }

            return new GroupQrPayload(organizationId, branchId, tableId, sessionId);
        } catch (AppException exception) {
            throw exception;
        } catch (ParseException | JOSEException | GeneralSecurityException exception) {
            throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
        }
    }

    private byte[] deriveGroupSecret(String sessionId) throws GeneralSecurityException {
        String scope = TableQrConstants.SECRET_SCOPE_GROUP_QR + sessionId;
        return hmac(qrSignerKey.getBytes(StandardCharsets.UTF_8), scope);
    }

    private byte[] hmac(byte[] key, String value) throws GeneralSecurityException {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(key, "HmacSHA512"));
        return mac.doFinal(value.getBytes(StandardCharsets.UTF_8));
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
