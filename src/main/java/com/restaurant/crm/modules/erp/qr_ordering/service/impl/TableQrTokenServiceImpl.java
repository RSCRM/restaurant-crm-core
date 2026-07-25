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
import com.restaurant.crm.modules.erp.qr_ordering.model.TableQrPayload;
import com.restaurant.crm.modules.erp.qr_ordering.service.interfaces.TableQrTokenService;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.experimental.NonFinal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.GeneralSecurityException;
import java.text.ParseException;
import java.util.Date;

/**
 * TABLE QR crypto (uc-c-02). Mirrors the HMAC/SignedJWT precedent in
 * {@code AttendanceServiceImpl}, but the secret is a stable per-branch scope
 * (never rotated by day) and the token carries no {@code exp}.
 */
@Slf4j
@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class TableQrTokenServiceImpl implements TableQrTokenService {

    @NonFinal
    @Value("${security.qr.signer-key:${security.jwt.signer-key}}")
    String qrSignerKey;

    @Override
    public String generate(TableQrPayload payload) {
        int qrVersion = payload.qrVersion() == null
                ? TableQrConstants.DEFAULT_QR_VERSION
                : payload.qrVersion();
        try {
            byte[] secret = deriveTableSecret(payload.organizationId(), payload.branchId());
            JWTClaimsSet claims = new JWTClaimsSet.Builder()
                    .issueTime(new Date())
                    .claim(JwtClaimSetConstant.CLAIM_TYPE, TableQrConstants.TOKEN_TYPE_TABLE_QR)
                    .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, payload.organizationId())
                    .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, payload.branchId())
                    .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, payload.tableId())
                    .claim(JwtClaimSetConstant.CLAIM_QR_VERSION, qrVersion)
                    .build();
            SignedJWT token = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            token.sign(new MACSigner(secret));
            return token.serialize();
        } catch (GeneralSecurityException | JOSEException exception) {
            throw new AppException(ErrorCode.TQR_GENERATION_FAILED);
        }
    }

    @Override
    public TableQrPayload verify(String token) {
        try {
            SignedJWT jwt = SignedJWT.parse(token);
            if (!JWSAlgorithm.HS512.equals(jwt.getHeader().getAlgorithm())) {
                throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
            }

            JWTClaimsSet claims = jwt.getJWTClaimsSet();
            if (!TableQrConstants.TOKEN_TYPE_TABLE_QR
                    .equals(claims.getStringClaim(JwtClaimSetConstant.CLAIM_TYPE))) {
                throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
            }

            String organizationId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID);
            String branchId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_BRANCH_ID);
            if (isBlank(organizationId) || isBlank(branchId)) {
                throw new AppException(ErrorCode.TQR_TOKEN_CLAIM_MISSING);
            }

            // Secret is derived from the (still untrusted) org/branch claims; a forged
            // org/branch changes the secret, so a bad signature is caught right here.
            byte[] secret = deriveTableSecret(organizationId, branchId);
            if (!jwt.verify(new MACVerifier(secret))) {
                throw new AppException(ErrorCode.TQR_TOKEN_SIGNATURE_MISMATCH);
            }

            String tableId = claims.getStringClaim(JwtClaimSetConstant.CLAIM_TABLE_ID);
            if (isBlank(tableId)) {
                throw new AppException(ErrorCode.TQR_TOKEN_CLAIM_MISSING);
            }

            Integer qrVersion = claims.getIntegerClaim(JwtClaimSetConstant.CLAIM_QR_VERSION);
            if (qrVersion == null) {
                qrVersion = TableQrConstants.DEFAULT_QR_VERSION;
            }
            // TODO(uc-c-02): so sánh với RestaurantTable.qrVersion khi owner module table
            // bổ sung cột qr_version; hiện chấp nhận mọi version.
            log.debug("TABLE QR verified for branch {} table {} (qrVersion={} not yet enforced)",
                    branchId, tableId, qrVersion);

            return new TableQrPayload(organizationId, branchId, tableId, qrVersion);
        } catch (AppException exception) {
            throw exception;
        } catch (ParseException | JOSEException | GeneralSecurityException exception) {
            throw new AppException(ErrorCode.TQR_TOKEN_INVALID);
        }
    }

    private byte[] deriveTableSecret(String organizationId, String branchId)
            throws GeneralSecurityException {
        String scope = TableQrConstants.SECRET_SCOPE_TABLE_QR + organizationId + ":" + branchId;
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
