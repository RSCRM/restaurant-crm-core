package com.restaurant.crm.modules.crm.customer_account.repository;

import com.restaurant.crm.common.redis.RedisKeyGenerator;
import com.restaurant.crm.modules.crm.customer_account.model.OtpCodeEntry;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The only gateway to the OTP keyspace in Redis (uc-c-03). Services never touch
 * {@code StringRedisTemplate} directly. Counting uses atomic {@code HINCRBY}/{@code INCR}
 * (never read-modify-write). Keys are built via {@code RedisKeyGenerator}.
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class OtpRedisRepository {

    private static final String FIELD_CODE_HMAC = "codeHmac";
    private static final String FIELD_ATTEMPTS = "attempts";
    private static final String FIELD_ISSUED_AT = "issuedAt";
    private static final String FIELD_BRANCH_ID = "branchId";
    private static final String FIELD_TABLE_ID = "tableId";

    StringRedisTemplate redisTemplate;

    // ==== OTP code hash ====

    /** Stores a fresh OTP code hash (attempts=0) with its TTL (uc-c-03). */
    public void saveCode(String phone, String codeHmac, String branchId, String tableId, long ttlSeconds) {
        Map<String, String> map = new HashMap<>();
        map.put(FIELD_CODE_HMAC, codeHmac);
        map.put(FIELD_ATTEMPTS, "0");
        map.put(FIELD_ISSUED_AT, Long.toString(Instant.now().toEpochMilli()));
        map.put(FIELD_BRANCH_ID, branchId);
        map.put(FIELD_TABLE_ID, tableId);

        String key = RedisKeyGenerator.generateOtpCodeKey(phone);
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /** Reads the current OTP code hash; empty if it expired or was never issued (uc-c-03). */
    public Optional<OtpCodeEntry> findCode(String phone) {
        Map<Object, Object> raw = redisTemplate.opsForHash()
                .entries(RedisKeyGenerator.generateOtpCodeKey(phone));
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new OtpCodeEntry(
                str(raw, FIELD_CODE_HMAC),
                parseInt(str(raw, FIELD_ATTEMPTS)),
                Instant.ofEpochMilli(Long.parseLong(str(raw, FIELD_ISSUED_AT))),
                str(raw, FIELD_BRANCH_ID),
                str(raw, FIELD_TABLE_ID)));
    }

    /** Atomically increments and returns the wrong-attempt count ({@code HINCRBY}) (uc-c-03). */
    public long incrementAttempts(String phone) {
        Long attempts = redisTemplate.opsForHash()
                .increment(RedisKeyGenerator.generateOtpCodeKey(phone), FIELD_ATTEMPTS, 1L);
        return attempts == null ? 0L : attempts;
    }

    /** Removes the OTP code hash (on success or after max attempts) (uc-c-03). */
    public void deleteCode(String phone) {
        redisTemplate.delete(RedisKeyGenerator.generateOtpCodeKey(phone));
    }

    // ==== phone lock (15 min) ====

    public void lockPhone(String phone, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                RedisKeyGenerator.generateOtpLockKey(phone), "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isPhoneLocked(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyGenerator.generateOtpLockKey(phone)));
    }

    public void unlockPhone(String phone) {
        redisTemplate.delete(RedisKeyGenerator.generateOtpLockKey(phone));
    }

    // ==== resend cooldown (60s) ====

    public void markResend(String phone, long ttlSeconds) {
        redisTemplate.opsForValue().set(
                RedisKeyGenerator.generateOtpResendKey(phone), "1", ttlSeconds, TimeUnit.SECONDS);
    }

    public boolean isResendBlocked(String phone) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(RedisKeyGenerator.generateOtpResendKey(phone)));
    }

    // ==== per-table rate limit (INCR) ====

    /**
     * Atomically increments and returns the per-table OTP request counter ({@code INCR}) (uc-c-03).
     * Sets the TTL window on the first request so the counter self-expires.
     */
    public long incrementTableCounter(String branchId, String tableId, long ttlSeconds) {
        String key = RedisKeyGenerator.generateOtpTableKey(branchId, tableId);
        Long count = redisTemplate.opsForValue().increment(key);
        if (count != null && count == 1L) {
            redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
        }
        return count == null ? 0L : count;
    }

    // ==== helpers ====

    private String str(Map<Object, Object> raw, String field) {
        Object value = raw.get(field);
        return value == null ? null : value.toString();
    }

    private int parseInt(String value) {
        return value == null ? 0 : Integer.parseInt(value);
    }
}
