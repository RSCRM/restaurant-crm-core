package com.restaurant.crm.common.redis;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class RedisKeyGenerator {

    private RedisKeyGenerator() {}

    public static String generateBlacklistKey(String token) {
        String hash = sha256(token);
        return RedisConstants.BLACKLIST_PREFIX + hash;
    }

    /** Owner-election pointer: {@code qr:table:{branchId}:{tableId}} → sessionId (uc-c-02). */
    public static String generateQrTableKey(String branchId, String tableId) {
        return RedisConstants.QR_TABLE_PREFIX + branchId + ":" + tableId;
    }

    /** Session hash: {@code qr:session:{sessionId}} (uc-c-02). */
    public static String generateQrSessionKey(String sessionId) {
        return RedisConstants.QR_SESSION_PREFIX + sessionId;
    }

    /** Members hash: {@code qr:session:{sessionId}:members} → deviceId → JSON (uc-c-02). */
    public static String generateQrSessionMembersKey(String sessionId) {
        return RedisConstants.QR_SESSION_PREFIX + sessionId + RedisConstants.QR_SESSION_MEMBERS_SUFFIX;
    }

    /** Reverse order pointer: {@code qr:order:{orderId}} → sessionId (uc-c-02, written by uc-c-05). */
    public static String generateQrOrderKey(String orderId) {
        return RedisConstants.QR_ORDER_PREFIX + orderId;
    }

    private static String sha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : hashBytes) {
                sb.append(String.format("%02x", b));
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("SHA-256 algorithm not available", e);
        }
    }
}
