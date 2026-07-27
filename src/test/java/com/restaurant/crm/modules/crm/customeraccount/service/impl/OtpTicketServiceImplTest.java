package com.restaurant.crm.modules.crm.customeraccount.service.impl;

import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.JWSHeader;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import com.restaurant.crm.common.constant.JwtClaimSetConstant;
import com.restaurant.crm.modules.crm.customeraccount.constants.CustomerOtpConstants;
import com.restaurant.crm.modules.crm.customeraccount.model.OtpTicketPayload;
import com.restaurant.crm.modules.crm.customeraccount.service.impl.OtpTicketServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class OtpTicketServiceImplTest {

    private static final String TEST_SIGNER_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";
    private static final String PHONE = "0987654321";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";

    private OtpTicketServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new OtpTicketServiceImpl();
        ReflectionTestUtils.setField(service, "otpSignerKey", TEST_SIGNER_KEY);
    }

    @Test
    void issueThenVerifyRoundTripReturnsPayload() {
        String ticket = service.issue(PHONE, BRANCH, TABLE).token();

        OtpTicketPayload payload = service.verify(ticket).orElseThrow();

        assertEquals(PHONE, payload.customerPhone());
        assertEquals(BRANCH, payload.branchId());
        assertEquals(TABLE, payload.tableId());
    }

    @Test
    void verifyRejectsExpiredTicket() {
        String ticket = signWithBranch(baseClaims().expirationTime(new Date(0)).build(), BRANCH);
        assertTrue(service.verify(ticket).isEmpty());
    }

    @Test
    void verifyRejectsWrongType() {
        String ticket = signWithBranch(
                baseClaims().claim(JwtClaimSetConstant.CLAIM_TYPE, "SOMETHING_ELSE")
                        .expirationTime(future()).build(),
                BRANCH);
        assertTrue(service.verify(ticket).isEmpty());
    }

    @Test
    void verifyRejectsTamperedSignature() {
        String ticket = service.issue(PHONE, BRANCH, TABLE).token();
        String[] parts = ticket.split("\\.");
        parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
        assertTrue(service.verify(String.join(".", parts)).isEmpty());
    }

    @Test
    void verifyRejectsTicketSignedWithAnotherBranchSecret() {
        // Claims say branch-1 but the token is signed with branch-2's secret.
        String ticket = signWithBranch(baseClaims().expirationTime(future()).build(), "branch-2");
        assertTrue(service.verify(ticket).isEmpty());
    }

    @Test
    void verifyRejectsGarbage() {
        assertTrue(service.verify("not-a-jwt").isEmpty());
    }

    private JWTClaimsSet.Builder baseClaims() {
        return new JWTClaimsSet.Builder()
                .jwtID("jti-1")
                .issueTime(new Date())
                .claim(JwtClaimSetConstant.CLAIM_TYPE, CustomerOtpConstants.TICKET_TOKEN_TYPE)
                .claim(JwtClaimSetConstant.CLAIM_CUSTOMER_PHONE, PHONE)
                .claim(JwtClaimSetConstant.CLAIM_BRANCH_ID, BRANCH)
                .claim(JwtClaimSetConstant.CLAIM_TABLE_ID, TABLE);
    }

    private Date future() {
        return new Date(System.currentTimeMillis() + 300_000);
    }

    private String signWithBranch(JWTClaimsSet claims, String branchForSecret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA512");
            mac.init(new SecretKeySpec(TEST_SIGNER_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA512"));
            byte[] secret = mac.doFinal(
                    (CustomerOtpConstants.TICKET_SECRET_SCOPE + branchForSecret).getBytes(StandardCharsets.UTF_8));
            SignedJWT jwt = new SignedJWT(new JWSHeader(JWSAlgorithm.HS512), claims);
            jwt.sign(new MACSigner(secret));
            return jwt.serialize();
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
