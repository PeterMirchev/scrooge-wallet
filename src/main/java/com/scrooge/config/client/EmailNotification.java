package com.scrooge.config.client;

import com.scrooge.web.dto.NotificationPreferenceCreateRequest;
import com.scrooge.web.dto.NotificationPreferenceResponse;
import com.scrooge.web.dto.NotificationRequest;
import com.scrooge.web.dto.NotificationResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@FeignClient(name = "notifications", url = "http://localhost:8081/api/v1/notifications")
public interface EmailNotification {

    @PostMapping("/notification")
    ResponseEntity<Void> sendNotification(@RequestBody NotificationRequest request);

    @PostMapping("/preference")
    ResponseEntity<NotificationPreferenceResponse> createNotificationPreference(@RequestBody NotificationPreferenceCreateRequest request);

    @PutMapping("/toggle")
    NotificationPreferenceResponse switchNotificationPreference(@RequestParam(name = "userId") UUID userId);

    @GetMapping("/preference")
    NotificationPreferenceResponse getNotificationPreference(@RequestParam(name = "userId") UUID userId);
}
