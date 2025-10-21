package com.scrooge.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrooge.model.enums.CaseStatus;
import com.scrooge.web.dto.CaseCache;
import com.scrooge.web.dto.Message;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

public class CaseCacheMapper {

    private static final ObjectMapper objectMapper = new ObjectMapper();

    public static CaseCache fromRedisToCaseCache(Map<Object, Object> map) {
        if (map == null || map.isEmpty()) return null;

        return CaseCache.builder()
                .id(parseUUID(map.get("id")))
                .caseOwner(parseUUID(map.get("caseOwner")))
                .requesterId(parseUUID(map.get("requesterId")))
                .requesterName((String) map.get("requesterName"))
                .requesterEmail((String) map.get("requesterEmail"))
                .caseName((String) map.get("caseName"))
                .caseDescription((String) map.get("caseDescription"))
                .createdOn(parseDate(map.get("createdOn")))
                .updatedOn(parseDate(map.get("updatedOn")))
                .status(parseStatus(map.get("status")))
                .messages(parseMessages(map.get("messages")))
                .build();
    }

    private static UUID parseUUID(Object value) {
        return value != null ? UUID.fromString(value.toString()) : null;
    }

    private static LocalDateTime parseDate(Object value) {
        return value != null ? LocalDateTime.parse(value.toString()) : null;
    }

    private static CaseStatus parseStatus(Object value) {
        return value != null ? CaseStatus.valueOf(value.toString()) : null;
    }

    @SuppressWarnings("unchecked")
    private static List<Message> parseMessages(Object messagesObj) {

        if (messagesObj == null) return Collections.emptyList();

        try {
            if (messagesObj instanceof List<?> list) {

                return list.stream()
                        .filter(Objects::nonNull)
                        .map(CaseCacheMapper::convertMessage)
                        .collect(Collectors.toList());
            } else if (messagesObj instanceof String json) {
                return objectMapper.readValue(json, new TypeReference<>() {});
            }
        } catch (Exception e) {
            System.err.println("Failed to parse messages: " + e.getMessage());
        }

        return Collections.emptyList();
    }

    private static Message convertMessage(Object obj) {
        if (obj instanceof Map<?, ?> map) {
            return Message.builder()
                    .id(parseUUID(map.get("id")))
                    .text((String) map.get("text"))
                    .author((String) map.get("author"))
                    .dateTime(parseDate(map.get("dateTime")))
                    .build();
        }
        return null;
    }
}
