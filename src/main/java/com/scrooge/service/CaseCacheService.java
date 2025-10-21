package com.scrooge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrooge.event.SupportCaseEventProducer;
import com.scrooge.web.dto.CaseCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class CaseCacheService {

    private static final Logger log = LoggerFactory.getLogger(CaseCacheService.class);

    private final SupportCaseEventProducer supportCaseEventProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CaseCacheService(SupportCaseEventProducer supportCaseEventProducer,
                            RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper) {
        this.supportCaseEventProducer = supportCaseEventProducer;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    /**
     * Retrieve all cases from Redis cache.
     */
    public List<CaseCache> getAllCases() {
        Set<String> keys = redisTemplate.keys("case:*");
        if (keys == null || keys.isEmpty()) {
            log.warn("No cases found in Redis cache");
            return Collections.emptyList();
        }

        return keys.stream()
                .map(this::getCaseSafe)
                .filter(Objects::nonNull)
                .toList();
    }

    /**
     * Retrieve a single case by ID from Redis cache.
     */
    public CaseCache getCase(UUID id) {

        String key = "case:" + id;
        String json = redisTemplate.opsForValue().get(key);

        if (json == null) {
            log.warn("Case with ID {} not found in Redis", id);
            return null;
        }

        try {
            return objectMapper.readValue(json, CaseCache.class);
        } catch (Exception e) {
            log.error("Failed to parse case JSON from Redis key {}", key, e);
            return null;
        }
    }


    private CaseCache getCaseSafe(String key) {

        try {
            String json = redisTemplate.opsForValue().get(key);
            return objectMapper.readValue(json, CaseCache.class);
        } catch (Exception e) {
            log.error("Failed to parse case JSON for key {}", key, e);
            return null;
        }
    }
}
