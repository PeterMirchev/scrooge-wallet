package com.scrooge.web.mapper;

import com.scrooge.model.enums.CaseStatus;
import com.scrooge.web.dto.CaseCache;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.Map;
import java.util.UUID;

public class CaseCacheMapper {

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
                .messages(Collections.emptyList()) // Replace with real messages if needed
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
}
