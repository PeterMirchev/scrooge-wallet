package com.scrooge.web.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationPreferenceResponse {

    private UUID id;
    private UUID userId;
    private boolean enableNotification;
    private String email;
    private LocalDateTime createdOn;
    private LocalDateTime updatedOn;
}
