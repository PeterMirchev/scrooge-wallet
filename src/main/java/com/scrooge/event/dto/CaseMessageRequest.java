package com.scrooge.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseMessageRequest {

    private UUID caseId;
    private String text;
    private String author;
    private LocalDateTime dateTime;
}
