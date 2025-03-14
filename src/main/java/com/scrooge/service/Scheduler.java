package com.scrooge.service;

import com.scrooge.config.client.EmailNotification;
import com.scrooge.model.User;
import com.scrooge.model.enums.NotificationType;
import com.scrooge.web.dto.NotificationRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class Scheduler {

    private final UserService userService;
    private final EmailNotification emailNotification;

    public Scheduler(UserService userService, EmailNotification emailNotification) {
        this.emailNotification = emailNotification;
        this.userService = userService;
    }

    @Scheduled(fixedDelay = 500000)
    public void notifyDeactivateUsers() {

        List<User> users = userService.getAllUsers();

        users.forEach(u -> {

            NotificationRequest notificationRequest = NotificationRequest.builder()
                    .userId(u.getId())
                    .email(u.getEmail())
                    .subject("Deactivated Account - Action Pending")
                    .body("Your account is deactivated. Please contact support.")
                    .type(NotificationType.NOTIFICATION)
                    .build();

            if (!u.isActive()){
                emailNotification.sendNotification(notificationRequest);
            }
        });
    }
}
