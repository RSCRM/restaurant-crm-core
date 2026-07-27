package com.restaurant.crm.modules.erp.order.repository;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.restaurant.crm.common.enums.ErrorCode;
import com.restaurant.crm.common.exception.AppException;
import com.restaurant.crm.common.redis.RedisKeyGenerator;
import com.restaurant.crm.modules.erp.order.model.GroupCartItem;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;


@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class GroupCartRedisRepository {

    StringRedisTemplate redisTemplate;
    ObjectMapper objectMapper = new ObjectMapper().findAndRegisterModules();

    // ==== cart items ====


    public void saveItem(String sessionId, GroupCartItem item, long ttlSeconds) {
        String key = RedisKeyGenerator.generateCartKey(sessionId);
        redisTemplate.opsForHash().put(key, item.cartItemId(), writeItem(item));
        redisTemplate.expire(key, ttlSeconds, TimeUnit.SECONDS);
    }


    public Optional<GroupCartItem> getItem(String sessionId, String cartItemId) {
        Object value = redisTemplate.opsForHash()
                .get(RedisKeyGenerator.generateCartKey(sessionId), cartItemId);
        return value == null ? Optional.empty() : Optional.of(readItem((String) value));
    }


    public List<GroupCartItem> getItems(String sessionId) {
        return redisTemplate.opsForHash()
                .values(RedisKeyGenerator.generateCartKey(sessionId))
                .stream()
                .map(value -> readItem((String) value))
                .toList();
    }


    public void removeItem(String sessionId, String cartItemId) {
        redisTemplate.opsForHash().delete(RedisKeyGenerator.generateCartKey(sessionId), cartItemId);
    }


    public void clearCart(String sessionId) {
        redisTemplate.delete(RedisKeyGenerator.generateCartKey(sessionId));
    }


    public void touchCartTtl(String sessionId, long ttlSeconds) {
        redisTemplate.expire(RedisKeyGenerator.generateCartKey(sessionId), ttlSeconds, TimeUnit.SECONDS);
    }

    // ==== item edit lock (BR-CST-GRP-03) ====


    public boolean tryLockItem(String sessionId, String cartItemId, String deviceId, long ttlSeconds) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                RedisKeyGenerator.generateCartLockKey(sessionId, cartItemId), deviceId, ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }


    public Optional<String> getLockOwner(String sessionId, String cartItemId) {
        return Optional.ofNullable(
                redisTemplate.opsForValue().get(RedisKeyGenerator.generateCartLockKey(sessionId, cartItemId)));
    }


    public void releaseLock(String sessionId, String cartItemId) {
        redisTemplate.delete(RedisKeyGenerator.generateCartLockKey(sessionId, cartItemId));
    }

    // ==== submit guard (duplicate-submission protection) ====


    public boolean tryAcquireSubmitGuard(String sessionId, String deviceId, long ttlSeconds) {
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                RedisKeyGenerator.generateCartSubmitGuardKey(sessionId), deviceId, ttlSeconds, TimeUnit.SECONDS);
        return Boolean.TRUE.equals(acquired);
    }


    public void releaseSubmitGuard(String sessionId) {
        redisTemplate.delete(RedisKeyGenerator.generateCartSubmitGuardKey(sessionId));
    }

    // ==== helpers ====

    private String writeItem(GroupCartItem item) {
        try {
            return objectMapper.writeValueAsString(item);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.SERVER_UNCATEGORIZED_EXCEPTION);
        }
    }

    private GroupCartItem readItem(String json) {
        try {
            return objectMapper.readValue(json, GroupCartItem.class);
        } catch (Exception exception) {
            throw new AppException(ErrorCode.CART_ITEM_NOT_FOUND);
        }
    }
}
