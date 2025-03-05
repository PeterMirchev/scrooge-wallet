package com.scrooge.config.client;

import com.scrooge.web.dto.NotificationPreferenceCreateRequest;
import com.scrooge.web.dto.NotificationPreferenceResponse;
import com.scrooge.web.dto.NotificationRequest;
import com.scrooge.web.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "notifications", url = "http://localhost:8081/api/v1/notifications")
public interface EmailNotification {

    @PostMapping("/notification")
    public ResponseEntity<NotificationResponse> sendNotification(@RequestBody NotificationRequest request);
    @PostMapping("/preference")
    public ResponseEntity<NotificationPreferenceResponse> createNotificationPreference(@RequestBody NotificationPreferenceCreateRequest request);
}
