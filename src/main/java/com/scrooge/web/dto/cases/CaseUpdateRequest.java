package com.scrooge.web.dto.cases;

import com.scrooge.model.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseUpdateRequest {

    private UUID caseId;
    private UUID userId;
    private String ownerEmail;
    private String caseName;
    private String description;
    private CaseStatus status;
}
