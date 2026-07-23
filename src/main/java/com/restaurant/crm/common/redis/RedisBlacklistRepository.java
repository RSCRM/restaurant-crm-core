package com.restaurant.crm.common.redis;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.concurrent.TimeUnit;

@Repository
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RedisBlacklistRepository {
    StringRedisTemplate stringRedisTemplate;

    public void save(String tokenHash, long ttlInSeconds) {
        stringRedisTemplate.opsForValue().set(tokenHash, "1", ttlInSeconds, TimeUnit.SECONDS);
    }

    public boolean exists(String tokenHash) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(tokenHash));
    }

    public void delete(String tokenHash) {
        stringRedisTemplate.delete(tokenHash);
    }
}
