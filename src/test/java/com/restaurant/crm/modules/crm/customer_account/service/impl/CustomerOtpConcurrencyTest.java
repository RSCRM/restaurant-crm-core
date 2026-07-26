package com.restaurant.crm.modules.crm.customer_account.service.impl;

import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.modules.crm.customer_account.model.OtpCodeEntry;
import com.restaurant.crm.modules.crm.customer_account.repository.CustomerRepository;
import com.restaurant.crm.modules.crm.customer_account.repository.OtpRedisRepository;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpSender;
import com.restaurant.crm.modules.crm.customer_account.service.interfaces.OtpTicketService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * BR-CST-ACC-02 / acceptance: 3 wrong verifies firing at the same instant must count to exactly 3
 * with no lost attempt, and the phone must be locked exactly once. The atomic {@code HINCRBY} is
 * modelled by an {@link AtomicLong} stand-in.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CustomerOtpConcurrencyTest {

    private static final String TEST_KEY =
            "9a4f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f2c8d3b7e1a5f";
    private static final String BRANCH = "branch-1";
    private static final String TABLE = "table-1";
    private static final String PHONE = "0987654321";
    private static final int THREADS = 3;

    @Mock CustomerRepository customerRepository;
    @Mock OtpRedisRepository otpRedisRepository;
    @Mock OtpTicketService otpTicketService;
    @Mock OtpSender otpSender;
    @InjectMocks CustomerOtpServiceImpl service;

    @Test
    void threeConcurrentWrongAttemptsCountToThreeAndLockOnce() throws InterruptedException {
        ReflectionTestUtils.setField(service, "otpSignerKey", TEST_KEY);
        when(otpRedisRepository.findCode(PHONE))
                .thenReturn(Optional.of(new OtpCodeEntry(hmac("999999"), 0, Instant.now(), BRANCH, TABLE)));

        AtomicLong attempts = new AtomicLong();
        when(otpRedisRepository.incrementAttempts(PHONE)).thenAnswer(invocation -> attempts.incrementAndGet());

        ExecutorService pool = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startGate = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(THREADS);

        for (int i = 0; i < THREADS; i++) {
            pool.submit(() -> {
                try {
                    startGate.await();
                    service.verify(PHONE, BRANCH, TABLE, "123456");
                } catch (AppException ignored) {
                    // every attempt is wrong → OTP_INVALID or OTP_MAX_ATTEMPTS
                } catch (InterruptedException interrupted) {
                    Thread.currentThread().interrupt();
                } finally {
                    done.countDown();
                }
            });
        }

        startGate.countDown();
        done.await();
        pool.shutdownNow();

        assertEquals(3L, attempts.get(), "all three wrong attempts must be counted");
        verify(otpRedisRepository, times(1)).lockPhone(eq(PHONE), anyLong());
        verify(otpRedisRepository, times(1)).deleteCode(PHONE);
    }

    private String hmac(String code) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(TEST_KEY.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return Base64.getEncoder().encodeToString(
                    mac.doFinal((PHONE + ":" + code).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException(exception);
        }
    }
}
