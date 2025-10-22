package com.scrooge.config;

import com.scrooge.config.client.emailnotificaion.EmailNotification;
import com.scrooge.model.User;
import com.scrooge.model.enums.Role;
import com.scrooge.service.AuditLogService;
import com.scrooge.service.UserService;
import com.scrooge.web.dto.notification.NotificationPreferenceCreateRequest;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static com.scrooge.service.UserService.mapNotificationPreferenceCreateRequest;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotification emailNotification;
    private final AuditLogService auditLogService;

    public DataInitializer(UserService userService, PasswordEncoder passwordEncoder, EmailNotification emailNotification, AuditLogService auditLogService) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.emailNotification = emailNotification;
        this.auditLogService = auditLogService;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {

        createAdminUser();
    }

    private void createAdminUser() {

        if (userService.getRootAdmin().isEmpty()) {

            User admin = User.builder()
                    .email("main.admin@scrooge.com")
                    .password(passwordEncoder.encode("123123"))
                    .role(Role.ADMINISTRATOR)
                    .active(true)
                    .createdOn(LocalDateTime.now())
                    .updatedOn(LocalDateTime.now())
                    .build();

            User persisted = userService.save(admin);

            try {
                NotificationPreferenceCreateRequest notificationPreferenceCreateRequest = mapNotificationPreferenceCreateRequest(persisted);
                emailNotification.createNotificationPreference(notificationPreferenceCreateRequest);
            } catch (Exception e) {
                auditLogService.log("NOTIFICATION_PREFERENCE_FAIL", "Unable to clreate notification preference.", admin);
            }

        }
    }
}
