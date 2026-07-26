package com.restaurant.crm.modules.erp.order.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.constants.TableQrConstants;
import com.restaurant.crm.modules.erp.order.model.TableQrPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class TableQrTokenServiceImplTest {

    private static final String TEST_SIGNER_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";
    private static final String ORG = "organization-1";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";

    private TableQrTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new TableQrTokenServiceImpl();
        ReflectionTestUtils.setField(service, "qrSignerKey", TEST_SIGNER_KEY);
    }

    @Test
    void generateThenVerifyRoundTripReturnsPayload() {
        String token = service.generate(new TableQrPayload(ORG, BRANCH, TABLE, 3));

        TableQrPayload payload = service.verify(token);

        assertEquals(ORG, payload.organizationId());
        assertEquals(BRANCH, payload.branchId());
        assertEquals(TABLE, payload.tableId());
        assertEquals(3, payload.qrVersion());
    }

    @Test
    void verifyDefaultsQrVersionWhenMissing() {
        String token = service.generate(new TableQrPayload(ORG, BRANCH, TABLE, null));

        assertEquals(TableQrConstants.DEFAULT_QR_VERSION, service.verify(token).qrVersion());
    }

    @Test
    void verifyRejectsTamperedSignature() {
        String token = service.generate(new TableQrPayload(ORG, BRANCH, TABLE, 1));
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
        String tampered = String.join(".", parts);

        AppException exception = assertThrows(AppException.class, () -> service.verify(tampered));
        assertEquals(ErrorCode.TQR_TOKEN_SIGNATURE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void verifyRejectsTokenSignedWithAnotherBranchSecret() throws Exception {
        // Claims say branch-1 but the token is signed with branch-2's derived secret.
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TableQrConstants.TOKEN_TYPE_TABLE_QR)
                .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, ORG)
                .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, BRANCH)
                .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, TABLE)
                .claim(JwtClaimSetConstant.CLAIM_QR_VERSION, 1)
                .build();
        String forged = signWithScope(claims,
                TableQrConstants.SECRET_SCOPE_TABLE_QR + ORG + ":branch-2");

        AppException exception = assertThrows(AppException.class, () -> service.verify(forged));
        assertEquals(ErrorCode.TQR_TOKEN_SIGNATURE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void verifyRejectsMissingTableIdClaim() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TableQrConstants.TOKEN_TYPE_TABLE_QR)
                .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, ORG)
                .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, BRANCH)
                .build();
        String token = signWithScope(claims,
                TableQrConstants.SECRET_SCOPE_TABLE_QR + ORG + ":" + BRANCH);

        AppException exception = assertThrows(AppException.class, () -> service.verify(token));
        assertEquals(ErrorCode.TQR_TOKEN_CLAIM_MISSING, exception.getErrorCode());
    }

    @Test
    void verifyRejectsWrongTokenType() throws Exception {
        JWTClaimsSet claims = new JWTClaimsSet.Builder()
                .issueTime(new Date())
                .claim(JwtClaimSetConstant.CLAIM_TYPE, TableQrConstants.TOKEN_TYPE_GROUP_QR)
                .claim(JwtClaimSetConstant.CLAIM_ORGANIZATION_ID, ORG)
                .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, BRANCH)
                .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, TABLE)
                .build();
        String token = signWithScope(claims,
                TableQrConstants.SECRET_SCOPE_TABLE_QR + ORG + ":" + BRANCH);

        AppException exception = assertThrows(AppException.class, () -> service.verify(token));
        assertEquals(ErrorCode.TQR_TOKEN_INVALID, exception.getErrorCode());
    }

    @Test
    void verifyRejectsGarbageToken() {
        AppException exception = assertThrows(AppException.class, () -> service.verify("not-a-jwt"));
        assertEquals(ErrorCode.TQR_TOKEN_INVALID, exception.getErrorCode());
    }

    private String signWithScope(JWTClaimsSet claims, String scope) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA512");
        mac.init(new SecretKeySpec(TEST_SIGNER_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
        byte[] secret = mac.doFinal(scope.getBytes(StandardCharsets.UTF_8));
        SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
        jwt.sign(new MACSigner(secret));
        return jwt.serialize();
    }
}
