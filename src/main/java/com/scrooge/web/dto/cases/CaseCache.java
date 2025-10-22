package com.scrooge.web.dto.cases;

import com.scrooge.model.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCache {

    private UUID id;
    private UUID caseOwner;
    private UUID requesterId;
    private String requesterName;
    private String requesterEmail;
    private String caseName;
    private String caseDescription;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
    private CaseStatus status;

    private List<Message> messages;
}
