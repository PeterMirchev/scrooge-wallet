package com.scrooge.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrooge.event.SupportCaseEventProducer;
import com.scrooge.event.dto.CaseMessageRequest;
import com.scrooge.event.dto.MessageCache;
import com.scrooge.model.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
public class MessageCacheService {

    private static final Logger log = LoggerFactory.getLogger(MessageCacheService.class);

    private final SupportCaseEventProducer eventProducer;
    private final RedisTemplate<String, String> redisTemplate;
    private final ObjectMapper objectMapper;

    public MessageCacheService(SupportCaseEventProducer eventProducer,
                               RedisTemplate<String, String> redisTemplate,
                               ObjectMapper objectMapper) {
        this.eventProducer = eventProducer;
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public List<MessageCache> getAllMessagesByCase(UUID caseId) {

        try {
            Set<String> messageKeys = redisTemplate.opsForSet().members(buildCaseMessagesKey(caseId));
            if (messageKeys.isEmpty()) {
                return Collections.emptyList();
            }

            return messageKeys.stream()
                    .map(this::getMessageSafe)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MessageCache::getDateTime))
                    .toList();
        } catch (Exception e) {
            log.error("Failed to get messages for case [{}] from Redis", caseId, e);
            return Collections.emptyList();
        }
    }

    public List<MessageCache> getAllMessages() {

        try {
            Set<String> messageKeys = redisTemplate.opsForSet().members("messages:all");
            if (messageKeys.isEmpty()) {
                log.info("No messages found in Redis");
                return Collections.emptyList();
            }

            return messageKeys.stream()
                    .map(this::getMessageSafe)
                    .filter(Objects::nonNull)
                    .sorted(Comparator.comparing(MessageCache::getDateTime))
                    .toList();

        } catch (Exception e) {
            log.error("Failed to get all cached messages from Redis", e);
            return Collections.emptyList();
        }
    }

    public void sendMessage(UUID caseId, User user, CaseMessageRequest messageRequest) {

        messageRequest.setCaseId(caseId);
        messageRequest.setAuthor(user.getEmail());
        messageRequest.setDateTime(LocalDateTime.now());
        eventProducer.sendMessageEvent(messageRequest);
    }

    public void sendMessage(UUID caseId, String author, String text) {

        CaseMessageRequest request = new CaseMessageRequest();
        request.setCaseId(caseId);
        request.setAuthor(author);
        request.setText(text);
        request.setDateTime(java.time.LocalDateTime.now());
        eventProducer.sendMessageEvent(request);
    }

    private MessageCache getMessageSafe(String key) {

        try {
            String json = redisTemplate.opsForValue().get(key);
            if (json == null) {
                return null;
            }
            return objectMapper.readValue(json, MessageCache.class);
        } catch (Exception e) {
            log.error("Failed to parse message JSON [{}]", key, e);
            return null;
        }
    }

    private String buildMessageKey(UUID messageId) {

        return "message:" + messageId;
    }

    private String buildCaseMessagesKey(UUID caseId) {

        return "case:" + caseId + ":messages";
    }
}
