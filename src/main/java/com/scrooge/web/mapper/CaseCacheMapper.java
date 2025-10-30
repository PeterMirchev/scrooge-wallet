package com.scrooge.web.mapper;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.scrooge.event.dto.CaseUpdateRequest;
import com.scrooge.model.User;
import com.scrooge.model.enums.CaseStatus;
import com.scrooge.event.dto.CaseCache;
import com.scrooge.event.dto.CaseMessageRequest;
import lombok.experimental.UtilityClass;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@UtilityClass
public class CaseCacheMapper {

    /**
     * Converts a CaseCache domain object to a CaseUpdateRequest DTO for web form binding.
     */
    public static CaseUpdateRequest toUpdateRequest(CaseCache caseCache) {

        if (caseCache == null) {
            return null;
        }

        CaseUpdateRequest dto = new CaseUpdateRequest();
        dto.setCaseName(caseCache.getCaseName());
        dto.setDescription(caseCache.getCaseDescription());
        dto.setStatus(caseCache.getStatus());
        return dto;
    }

    /**
     * Converts a MessageCache domain object to a CaseMessageRequest DTO for web form binding.
     */

    public static CaseMessageRequest toMessageRequest(User user) {

        return CaseMessageRequest.builder()
                .author(user.getEmail())
                .dateTime(LocalDateTime.now())
                .build();
    }
}
