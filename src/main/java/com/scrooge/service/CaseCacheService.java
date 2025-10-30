package com.scrooge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrooge.event.SupportCaseEventProducer;
import com.scrooge.event.dto.CaseMessageRequest;
import com.scrooge.model.User;
import com.scrooge.event.dto.CaseCache;
import com.scrooge.event.dto.CaseUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CaseCacheService {

    private static final Logger log = LoggerFactory.getLogger(CaseCacheService.class);

    private final SupportCaseEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public CaseCacheService(SupportCaseEventProducer eventProducer, RedisTemplate<String, String> redisTemplate,
                            ObjectMapper objectMapper) {
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<CaseCache> getAllCases() {

        Set<String> keys = redisTemplate.keys("case:*");
        if (keys == null || keys.isEmpty()) {
            log.warn("No cases found in Redis cache");
            return Collections.emptyList();
        }

        return keys.stream()
                .filter(key -> !key.endsWith(":messages"))
                .map(this::getCaseSafe)
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(CaseCache::getCreatedOn).reversed())
                .toList();
    }

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

    public void updateCase(UUID caseId, User user, CaseUpdateRequest caseUpdateRequest) {

        caseUpdateRequest.setCaseId(caseId);
        caseUpdateRequest.setOwnerEmail(user.getEmail());
        caseUpdateRequest.setUserId(user.getId());

        eventProducer.sendUpdateEvent(caseUpdateRequest);
    }
}
