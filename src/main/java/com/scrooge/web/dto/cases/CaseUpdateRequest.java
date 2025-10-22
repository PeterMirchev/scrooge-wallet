package com.scrooge.web.dto.cases;

import com.scrooge.model.enums.CaseStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseUpdateRequest {

    private String ownerEmail;
    private String caseName;
    private String description;
    private CaseStatus status;
}
