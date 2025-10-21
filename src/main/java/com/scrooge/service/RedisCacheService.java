package com.scrooge.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class RedisCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    public void cacheSupportCases(String userId, Object data) {
        String key = "cases:" + userId;
        redisTemplate.opsForValue().set(key, data, Duration.ofMinutes(5));
    }

    public Object getCachedSupportCases(String userId) {
        return redisTemplate.opsForValue().get("cases:" + userId);
    }

    public void evictCases(String userId) {
        redisTemplate.delete("cases:" + userId);
    }
}