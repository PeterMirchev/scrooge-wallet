package com.scrooge.web.dto.notification;

import lombok.Builder;
import lombok.Data;
import org.springframework.lang.NonNull;

import java.util.UUID;

@Data
@Builder
public class NotificationPreferenceUpdateRequest {

    @NonNull
    private UUID userId;
    @NonNull
    private boolean enableNotification;
    @NonNull
    private String email;
}
