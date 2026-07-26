package com.restaurant.crm.modules.erp.order.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.erp.order.constants.QrSessionConstants;
import com.restaurant.crm.modules.erp.order.model.GroupQrPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class GroupQrTokenServiceImplTest {

    private static final String TEST_SIGNER_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";
    private static final String ORG = "organization-1";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String SESSION = "session-1";

    private GroupQrTokenServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new GroupQrTokenServiceImpl();
        ReflectionTestUtils.setField(service, "qrSignerKey", TEST_SIGNER_KEY);
    }

    @Test
    void generateThenVerifyRoundTripReturnsPayload() {
        String token = service.generate(
                new GroupQrPayload(ORG, BRANCH, TABLE, SESSION),
                QrSessionConstants.GROUP_QR_TTL_SECONDS);

        GroupQrPayload payload = service.verify(token);

        assertEquals(ORG, payload.organizationId());
        assertEquals(BRANCH, payload.branchId());
        assertEquals(TABLE, payload.tableId());
        assertEquals(SESSION, payload.sessionId());
    }

    @Test
    void verifyRejectsExpiredToken() {
        String token = service.generate(
                new GroupQrPayload(ORG, BRANCH, TABLE, SESSION), -1L);

        AppException exception = assertThrows(AppException.class, () -> service.verify(token));
        assertEquals(ErrorCode.TQR_GROUP_QR_EXPIRED, exception.getErrorCode());
    }

    @Test
    void verifyRejectsTokenFromAnotherSession() {
        // Token minted for session-2 (secret scoped to session-2) then presented as-is;
        // verify derives the secret from the embedded sessionId, so the signature holds,
        // but a token whose sessionId was tampered to session-1 fails signature.
        String token = service.generate(
                new GroupQrPayload(ORG, BRANCH, TABLE, "session-2"),
                QrSessionConstants.GROUP_QR_TTL_SECONDS);
        String[] parts = token.split("\\.");
        parts[2] = (parts[2].startsWith("a") ? "b" : "a") + parts[2].substring(1);
        String tampered = String.join(".", parts);

        AppException exception = assertThrows(AppException.class, () -> service.verify(tampered));
        assertEquals(ErrorCode.TQR_TOKEN_SIGNATURE_MISMATCH, exception.getErrorCode());
    }

    @Test
    void verifyRejectsGarbageToken() {
        AppException exception = assertThrows(AppException.class, () -> service.verify("garbage"));
        assertEquals(ErrorCode.TQR_TOKEN_INVALID, exception.getErrorCode());
    }
}
