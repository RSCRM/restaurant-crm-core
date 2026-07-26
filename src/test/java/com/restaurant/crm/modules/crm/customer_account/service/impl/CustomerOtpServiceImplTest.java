package com.restaurant.crm.modules.crm.customer_account.service.impl;

import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.entity.Customer;
import com.restaurant.crm.modules.crm.customer_account.enums.CustomerStatus;
import com.restaurant.crm.modules.crm.customer_account.model.OtpCodeEntry;
import com.restaurant.crm.modules.crm.customer_account.model.OtpRequestResult;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customer_account.repository.OtpRedisRepository;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpTicketService;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerOtpServiceImplTest {

    private static final String TEST_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String PHONE = "0987654321";
    private static final String CODE = "123456";

    @Mock CustomerRepository customerRepository;
    @Mock OtpRedisRepository otpRedisRepository;
    @Mock OtpTicketService otpTicketService;
    @Mock OtpSender otpSender;
    @InjectMocks CustomerOtpServiceImpl service;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(service, "otpSignerKey", TEST_KEY);
    }

    // ==== request ====

    @Test
    void requestSendsCodeAndReturnsMaskedResult() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(otpRedisRepository.isPhoneLocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.isResendBlocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.incrementTableCounter(eq(BRANCH), eq(TABLE), anyLong())).thenReturn(1L);

        OtpRequestResult result = service.request("+84987654321", BRANCH, TABLE);

        assertEquals("0987***321", result.maskedPhone());
        verify(otpRedisRepository).saveCode(eq(PHONE), anyString(), eq(BRANCH), eq(TABLE), anyLong());
        verify(otpSender).send(eq(PHONE), anyString());
        verify(otpRedisRepository).markResend(eq(PHONE), anyLong());
    }

    @Test
    void requestRejectsInvalidPhone() {
        AppException exception = assertThrows(AppException.class,
                () -> service.request("0123456789", BRANCH, TABLE));

        assertEquals(ErrorCode.CUSTOMER_PHONE_INVALID, exception.getErrorCode());
        verify(otpSender, never()).send(anyString(), anyString());
        verify(otpRedisRepository, never()).saveCode(anyString(), anyString(), anyString(), anyString(), anyLong());
    }

    @Test
    void requestRejectsLockedCustomer() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.of(
                Customer.builder().phone(PHONE).status(CustomerStatus.LOCKED).build()));

        AppException exception = assertThrows(AppException.class,
                () -> service.request(PHONE, BRANCH, TABLE));

        assertEquals(ErrorCode.OTP_CUSTOMER_LOCKED, exception.getErrorCode());
        verify(otpSender, never()).send(anyString(), anyString());
    }

    @Test
    void requestRejectsWhenPhoneLocked() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(otpRedisRepository.isPhoneLocked(PHONE)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> service.request(PHONE, BRANCH, TABLE));

        assertEquals(ErrorCode.OTP_PHONE_LOCKED, exception.getErrorCode());
    }

    @Test
    void requestRejectsResendTooSoon() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(otpRedisRepository.isPhoneLocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.isResendBlocked(PHONE)).thenReturn(true);

        AppException exception = assertThrows(AppException.class,
                () -> service.request(PHONE, BRANCH, TABLE));

        assertEquals(ErrorCode.OTP_RESEND_TOO_SOON, exception.getErrorCode());
    }

    @Test
    void requestRejectsTableRateLimit() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(otpRedisRepository.isPhoneLocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.isResendBlocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.incrementTableCounter(eq(BRANCH), eq(TABLE), anyLong())).thenReturn(21L);

        AppException exception = assertThrows(AppException.class,
                () -> service.request(PHONE, BRANCH, TABLE));

        assertEquals(ErrorCode.OTP_TABLE_RATE_LIMIT, exception.getErrorCode());
        verify(otpSender, never()).send(anyString(), anyString());
    }

    @Test
    void requestDoesNotBurnCooldownWhenSendFails() {
        when(customerRepository.findByPhone(PHONE)).thenReturn(Optional.empty());
        when(otpRedisRepository.isPhoneLocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.isResendBlocked(PHONE)).thenReturn(false);
        when(otpRedisRepository.incrementTableCounter(eq(BRANCH), eq(TABLE), anyLong())).thenReturn(1L);
        doThrow(new AppException(ErrorCode.OTP_SEND_FAILED))
                .when(otpSender).send(eq(PHONE), anyString());

        AppException exception = assertThrows(AppException.class,
                () -> service.request(PHONE, BRANCH, TABLE));

        assertEquals(ErrorCode.OTP_SEND_FAILED, exception.getErrorCode());
        verify(otpRedisRepository).saveCode(eq(PHONE), anyString(), eq(BRANCH), eq(TABLE), anyLong());
        verify(otpRedisRepository, never()).markResend(anyString(), anyLong()); // send failed → no cooldown
        verify(otpRedisRepository, never()).deleteCode(anyString());            // stored code kept
    }

    // ==== verify ====

    @Test
    void verifyReturnsTicketAndClearsCodeOnCorrectCode() {
        when(otpRedisRepository.findCode(PHONE))
                .thenReturn(Optional.of(entry(hmac(PHONE, CODE), BRANCH, TABLE)));
        when(otpTicketService.issue(PHONE, BRANCH, TABLE))
                .thenReturn(new OtpTicketService.IssuedTicket("otp-ticket", Instant.now().plusSeconds(300)));

        String ticket = service.verify(PHONE, BRANCH, TABLE, CODE);

        assertEquals("otp-ticket", ticket);
        verify(otpRedisRepository).deleteCode(PHONE);
        verify(otpRedisRepository).unlockPhone(PHONE);
    }

    @Test
    void verifyRejectsWhenNoCode() {
        when(otpRedisRepository.findCode(PHONE)).thenReturn(Optional.empty());

        AppException exception = assertThrows(AppException.class,
                () -> service.verify(PHONE, BRANCH, TABLE, CODE));

        assertEquals(ErrorCode.OTP_EXPIRED, exception.getErrorCode());
    }

    @Test
    void verifyRejectsContextMismatch() {
        when(otpRedisRepository.findCode(PHONE))
                .thenReturn(Optional.of(entry(hmac(PHONE, CODE), BRANCH, "other-table")));

        AppException exception = assertThrows(AppException.class,
                () -> service.verify(PHONE, BRANCH, TABLE, CODE));

        assertEquals(ErrorCode.OTP_CONTEXT_MISMATCH, exception.getErrorCode());
    }

    @Test
    void verifyWrongCodeFirstAttemptDoesNotLock() {
        when(otpRedisRepository.findCode(PHONE))
                .thenReturn(Optional.of(entry(hmac(PHONE, "999999"), BRANCH, TABLE)));
        when(otpRedisRepository.incrementAttempts(PHONE)).thenReturn(1L);

        AppException exception = assertThrows(AppException.class,
                () -> service.verify(PHONE, BRANCH, TABLE, CODE));

        assertEquals(ErrorCode.OTP_INVALID, exception.getErrorCode());
        verify(otpRedisRepository, never()).lockPhone(anyString(), anyLong());
        verify(otpRedisRepository, never()).deleteCode(anyString());
    }

    @Test
    void verifyWrongCodeThirdAttemptLocksPhone() {
        when(otpRedisRepository.findCode(PHONE))
                .thenReturn(Optional.of(entry(hmac(PHONE, "999999"), BRANCH, TABLE)));
        when(otpRedisRepository.incrementAttempts(PHONE)).thenReturn(3L);

        AppException exception = assertThrows(AppException.class,
                () -> service.verify(PHONE, BRANCH, TABLE, CODE));

        assertEquals(ErrorCode.OTP_MAX_ATTEMPTS, exception.getErrorCode());
        verify(otpRedisRepository).deleteCode(PHONE);
        verify(otpRedisRepository).lockPhone(eq(PHONE), anyLong());
    }

    // ==== fixtures ====

    private OtpCodeEntry entry(String codeHmac, String branchId, String tableId) {
        return new OtpCodeEntry(codeHmac, 0, Instant.now(), branchId, tableId);
    }

    private String hmac(String phone, String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(
                    mac.doFinal((phone + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
