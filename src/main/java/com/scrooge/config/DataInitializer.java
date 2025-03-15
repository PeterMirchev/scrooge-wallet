package com.scrooge.config;

import com.scrooge.model.User;
import com.scrooge.model.enums.Role;
import com.scrooge.service.UserService;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.ApplicationListener;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(UserService userService, PasswordEncoder passwordEncoder) {
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
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

            userService.save(admin);
        }
    }
}
