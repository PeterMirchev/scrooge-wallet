package com.scrooge.event.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SupportCaseEvent {

    private UUID requesterId;
    private String requesterName;
    private String requesterEmail;
    private String description;
}
