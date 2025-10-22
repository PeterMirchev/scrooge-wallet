package com.scrooge.web.dto.notification;

import com.scrooge.model.enums.NotificationType;
import com.scrooge.model.enums.SendStatus;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID userId;
    private String subject;
    private NotificationType type;
    private SendStatus status;
    private LocalDateTime sent;
}
