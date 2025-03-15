package com.scrooge.service;

import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.scrooge.config.client.EmailNotification;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.model.enums.Role;
import com.scrooge.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

@ExtendWith(MockitoExtension.class)
class UserServiceUTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private AuditLogService auditLogService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private EmailNotification emailNotification;
    @InjectMocks
    private UserService userService;


    @Test
    void whenGetUserAdmin_thenReturnUserAdmin() {

        String email = "test@email.com";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(Role.ADMINISTRATOR)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.getRootAdmin();

        assertNotNull(user);
    }

    @Test
    void whenGetUserByEmail_thenReturnUser() {

        String email = "test@email.com";

        User user = User.builder()
                .id(UUID.randomUUID())
                .email(email)
                .role(Role.ADMINISTRATOR)
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        userService.getUserByEmail(email);

        assertNotNull(user);
    }

    @Test
    void whenGetUserById_thenReturnUser() {

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        User foundUser = userService.getUserById(userId);

        assertNotNull(foundUser);
        assertEquals(userId, foundUser.getId());
    }

    @Test
    void whenGetUserById_thenThrowResourceNotFoundException() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> {
            userService.getUserById(userId);
        });

        String expectedMessage = String.format(INVALID_USER_ID, userId);
        assertEquals(expectedMessage, exception.getMessage());
    }
}
