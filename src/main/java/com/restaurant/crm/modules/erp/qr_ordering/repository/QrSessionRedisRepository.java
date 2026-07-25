package com.restaurant.crm.modules.erp.qr_ordering.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.redis.RedisKeyGenerator;
import com.restaurant.crm.modules.erp.qr_ordering.enums.QrSessionStatus;
import com.restaurant.crm.modules.erp.qr_ordering.model.QrSessionData;
import com.restaurant.crm.modules.erp.qr_ordering.model.QrSessionMember;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

/**
 * The only gateway to the QR ordering session keyspace in Redis (uc-c-02).
 * Services never touch {@code StringRedisTemplate} directly. Keys are built via
 * {@code RedisKeyGenerator}; nothing is hardcoded here except the hash field names.
 */
@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class QrSessionRedisRepository {

    private static final String FIELD_ORGANIZATION_ID = "organizationId";
    private static final String FIELD_BRANCH_ID = "branchId";
    private static final String FIELD_TABLE_ID = "tableId";
    private static final String FIELD_OWNER_DEVICE_ID = "ownerDeviceId";
    private static final String FIELD_OWNER_CUSTOMER_ID = "ownerCustomerId";
    private static final String FIELD_OWNER_CUSTOMER_PHONE = "ownerCustomerPhone";
    private static final String FIELD_ORDER_ID = "orderId";
    private static final String FIELD_STATUS = "status";
    private static final String FIELD_CREATED_AT = "createdAt";

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // ==== Owner election (table pointer) ====

    /**
     * Atomically claims the table for a new session (SETNX). Winner becomes OWNER (uc-c-02).
     *
     * @return {@code true} if the table was free and is now reserved for {@code sessionId}
     */
    public boolean tryReserveTable(String branchId, String tableId, String sessionId, long ttlSeconds) {
        Boolean reserved = redisTemplate.opsForValue().setIfAbsent(
                RedisKeyGenerator.generateQrTableKey(branchId, tableId),
                sessionId, ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(reserved);
    }

    /** Returns the active sessionId reserving a table, if any (read-only; used by resolve). */
    public Optional<String> getTableSessionId(String branchId, String tableId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(RedisKeyGenerator.generateQrTableKey(branchId, tableId)));
    }

    /** Releases the table pointer (rollback on failed start, or on session close). */
    public void releaseTable(String branchId, String tableId) {
        redisTemplate.delete(RedisKeyGenerator.generateQrTableKey(branchId, tableId));
    }

    // ==== Session hash ====

    /** Persists (or overwrites) the session hash and (re)sets its TTL (uc-c-02). */
    public void saveSession(QrSessionData data, long ttlSeconds) {
        Map<String, String> map = new HashMap<>();
        map.put(FIELD_ORGANIZATION_ID, data.organizationId());
        map.put(FIELD_BRANCH_ID, data.branchId());
        map.put(FIELD_TABLE_ID, data.tableId());
        map.put(FIELD_OWNER_DEVICE_ID, data.ownerDeviceId());
        putIfPresent(map, FIELD_OWNER_CUSTOMER_ID, data.ownerCustomerId());
        putIfPresent(map, FIELD_OWNER_CUSTOMER_PHONE, data.ownerCustomerPhone());
        putIfPresent(map, FIELD_ORDER_ID, data.orderId());
        map.put(FIELD_STATUS, data.status().name());
        map.put(FIELD_CREATED_AT, Long.toString(data.createdAt().toEpochMilli()));

        String key = RedisKeyGenerator.generateQrSessionKey(data.sessionId());
        redisTemplate.opsForHash().putAll(key, map);
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /** Reads the session hash back into a typed record; empty if the session is gone/expired. */
    public Optional<QrSessionData> findSession(String sessionId) {
        Map<Object, Object> raw = redisTemplate.opsForHash()
                .entries(RedisKeyGenerator.generateQrSessionKey(sessionId));
        if (raw.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(new QrSessionData(
                sessionId,
                str(raw, FIELD_ORGANIZATION_ID),
                str(raw, FIELD_BRANCH_ID),
                str(raw, FIELD_TABLE_ID),
                str(raw, FIELD_OWNER_DEVICE_ID),
                str(raw, FIELD_OWNER_CUSTOMER_ID),
                str(raw, FIELD_OWNER_CUSTOMER_PHONE),
                str(raw, FIELD_ORDER_ID),
                QrSessionStatus.valueOf(str(raw, FIELD_STATUS)),
                Instant.ofEpochMilli(Long.parseLong(str(raw, FIELD_CREATED_AT)))));
    }

    /** Overwrites the session status field, keeping the current TTL. */
    public void updateStatus(String sessionId, QrSessionStatus status) {
        redisTemplate.opsForHash().put(
                RedisKeyGenerator.generateQrSessionKey(sessionId), FIELD_STATUS, status.name());
    }

    // ==== Members hash ====

    /** Adds/overwrites a member entry (JSON) and (re)sets the members hash TTL. */
    public void saveMember(String sessionId, QrSessionMember member, long ttlSeconds) {
        String key = RedisKeyGenerator.generateQrSessionMembersKey(sessionId);
        redisTemplate.opsForHash().put(key, member.deviceId(), writeMember(member));
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }

    /** Number of participants currently in the session. */
    public int countMembers(String sessionId) {
        Long size = redisTemplate.opsForHash()
                .size(RedisKeyGenerator.generateQrSessionMembersKey(sessionId));
        return size == null ? 0 : size.intValue();
    }

    /** All participants of the session. */
    public List<QrSessionMember> getMembers(String sessionId) {
        return redisTemplate.opsForHash()
                .values(RedisKeyGenerator.generateQrSessionMembersKey(sessionId))
                .stream()
                .map(value -> readMember((String) value))
                .toList();
    }

    /** A single participant by deviceId, if present. */
    public Optional<QrSessionMember> getMember(String sessionId, String deviceId) {
        Object value = redisTemplate.opsForHash()
                .get(RedisKeyGenerator.generateQrSessionMembersKey(sessionId), deviceId);
        return value == null ? Optional.empty() : Optional.of(readMember((String) value));
    }

    // ==== TTL / order binding ====

    /**
     * Extends the TTL of all four keys of a session (uc-c-02 heartbeat):
     * the table pointer, the session hash, the members hash, and the order pointer (if bound).
     */
    public void touchTtl(QrSessionData session, long ttlSeconds) {
        redisTemplate.expire(
                RedisKeyGenerator.generateQrTableKey(session.branchId(), session.tableId()),
                ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.expire(
                RedisKeyGenerator.generateQrSessionKey(session.sessionId()), ttlSeconds, TimeUnit.SECONDS);
        redisTemplate.expire(
                RedisKeyGenerator.generateQrSessionMembersKey(session.sessionId()), ttlSeconds, TimeUnit.SECONDS);
        if (session.orderId() != null && !session.orderId().isBlank()) {
            redisTemplate.expire(
                    RedisKeyGenerator.generateQrOrderKey(session.orderId()), ttlSeconds, TimeUnit.SECONDS);
        }
    }

    /**
     * Links a session to the DB order both ways (uc-c-05 calls this after {@code OrderService.create()}):
     * writes {@code orderId} into the session hash and {@code qr:order:{orderId}} → sessionId.
     * Idempotent: re-binding the same order is a no-op; re-binding a different order to a session
     * that already has one is rejected with {@code TQR_CONTEXT_MISMATCH}.
     */
    public void bindOrder(String sessionId, String orderId, long ttlSeconds) {
        String sessionKey = RedisKeyGenerator.generateQrSessionKey(sessionId);
        Object existing = redisTemplate.opsForHash().get(sessionKey, FIELD_ORDER_ID);
        if (existing != null && !existing.toString().isBlank()) {
            if (!existing.toString().equals(orderId)) {
                throw new AppException(ErrorCode.TQR_CONTEXT_MISMATCH);
            }
            return; // already bound to the same order — idempotent
        }
        redisTemplate.opsForHash().put(sessionKey, FIELD_ORDER_ID, orderId);
        redisTemplate.opsForValue().set(
                RedisKeyGenerator.generateQrOrderKey(orderId), sessionId, ttlSeconds, TimeUnit.SECONDS);
    }

    // ==== helpers ====

    private void putIfPresent(Map<String, String> map, String field, String value) {
        if (value != null && !value.isBlank()) {
            map.put(field, value);
        }
    }

    private String str(Map<Object, Object> raw, String field) {
        Object value = raw.get(field);
        return value == null ? null : value.toString();
    }

    private String writeMember(QrSessionMember member) {
        try {
            return objectMapper.writeValueAsString(member);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.TQR_GENERATION_FAILED);
        }
    }

    private QrSessionMember readMember(String json) {
        try {
            return objectMapper.readValue(json, QrSessionMember.class);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.TQR_SESSION_NOT_FOUND);
        }
    }
}
