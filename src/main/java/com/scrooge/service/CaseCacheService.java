package com.scrooge.service;

import com.scrooge.event.SupportCaseEventProducer;
import com.scrooge.model.enums.CaseStatus;
import com.scrooge.web.dto.CaseCache;
import com.scrooge.web.mapper.CaseCacheMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class CaseCacheService {

    private static final Logger log = LoggerFactory.getLogger(CaseCacheService.class);

    private final SupportCaseEventProducer supportCaseEventProducer;
    private final RedisTemplate<String, Object> redisTemplate;

    public CaseCacheService(SupportCaseEventProducer supportCaseEventProducer, RedisTemplate<String, Object> redisTemplate) {
        this.supportCaseEventProducer = supportCaseEventProducer;
        this.redisTemplate = redisTemplate;
    }

    public List<CaseCache> getAllCases() {

        Set<String> keys = redisTemplate.keys("case:*");
        if (keys == null || keys.isEmpty()) {
            log.warn("No cases found in Redis cache");
            return Collections.emptyList();
        }

        List<CaseCache> cases = keys.stream()
                .map(key -> {
                    Map<Object, Object> map = redisTemplate.opsForHash().entries(key);
                    return CaseCacheMapper.fromRedisToCaseCache(map);
                })
                .filter(Objects::nonNull)
                .toList();

        log.info("Retrieved {} cases from Redis cache", cases.size());
        return cases;
    }



    public CaseCache getCase(UUID id) {

        String key = "case:" + id;

        Map<Object, Object> map = redisTemplate.opsForHash().entries(key);

        if (map.isEmpty()) {
            log.warn("Case with ID {} not found in Redis", id);
            return null;
        }

        return CaseCacheMapper.fromRedisToCaseCache(map);
    }
}
