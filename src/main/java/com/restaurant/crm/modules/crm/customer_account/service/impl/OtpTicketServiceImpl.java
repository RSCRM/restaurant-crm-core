package com.restaurant.crm.modules.crm.customer_account.service.impl;

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
import com.restaurant.crm.modules.crm.customer_account.constants.CustomerOtpConstants;
import com.restaurant.crm.modules.crm.customer_account.model.OtpTicketPayload;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpTicketService;
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
import java.util.Optional;
import java.util.UUID;

/**
 * OTP ticket crypto (uc-c-03). Mirrors {@code TableQrTokenServiceImpl}: HS512 SignedJWT,
 * secret derived per branch, read-branch-claim-then-verify, exception-to-empty on the verify side.
 */
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpTicketServiceImpl implements OtpTicketService {

    @NonFinal
    @Value("${security.otp.signer-key:${security.jwt.signer-key}}")
    String otpSignerKey;

    @Override
    public IssuedTicket issue(String customerPhone, String branchId, String tableId) {
        Instant now = Instant.now();
        Instant expiresAt = now.plusSeconds(CustomerOtpConstants.TICKET_TTL_SECONDS);
        try {
            byte[] secret = deriveTicketSecret(branchId);
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .jwtID(UUID.randomUUID().toString())
                    .issueTime(Date.from(now))
                    .expirationTime(Date.from(expiresAt))
                    .claim(JwtClaimSetConstant.CLAIM_TYPE, CustomerOtpConstants.TICKET_TOKEN_TYPE)
                    .claim(JwtClaimSetConstant.CLAIM_CUSTOMER_PHONE, customerPhone)
                    .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, branchId)
                    .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, tableId)
                    .build();
            SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            token.sign(new MACSigner(secret));
            return new IssuedTicket(token.serialize(), expiresAt);
        } catch (GeneralSecurityException | JOSEException exception) {
            throw new AppException(ErrorCode.OTP_TICKET_GENERATION_FAILED);
        }
    }

    @Override
    public Optional<OtpTicketPayload> verify(String otpTicket) {
        try {
            SignedJWT jwt = SignedJWT.parse(otpTicket);
            if (!JWSAlgorithm.HS512.equals(jwt.getHeader().getAlgorithm())) {
                return Optional.empty();
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!CustomerOtpConstants.TICKET_TOKEN_TYPE
                    .equals(claims.getStringClaim(JwtClaimSetConstant.CLAIM_TYPE))) {
                return Optional.empty();
            }

            String branchId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID);
            if (isBlank(branchId)) {
                return Optional.empty();
            }

            byte[] secret = deriveTicketSecret(branchId);
            if (!jwt.verify(new MACVerifier(secret))) {
                return Optional.empty();
            }

            Date expiration = claims.getExpirationTime();
            if (expiration == null || !expiration.toInstant().isAfter(Instant.now())) {
                return Optional.empty();
            }

            String customerPhone = claims.getStringClaim(JwtClaimSetConstant.CLAIM_CUSTOMER_PHONE);
            String tableId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_TABLE_ID);
            if (isBlank(customerPhone) || isBlank(tableId)) {
                return Optional.empty();
            }

            return Optional.of(new OtpTicketPayload(customerPhone, branchId, tableId));
        } catch (ParseException | JOSEException | GeneralSecurityException exception) {
            return Optional.empty();
        }
    }

    private byte[] deriveTicketSecret(String branchId) throws GeneralSecurityException {
        String scope = CustomerOtpConstants.TICKET_SECRET_SCOPE + branchId;
        return hmac(otpSignerKey.getBytes(StandardCharsets.UTF_8), scope);
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
