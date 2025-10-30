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
public class MessageCache {

    private UUID id;
    private String text;
    private String author;
    private LocalDateTime dateTime;
    private UUID caseId;
}
