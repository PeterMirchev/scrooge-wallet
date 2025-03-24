package com.scrooge.service;

import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.scrooge.config.client.emailnotificaion.EmailNotification;
import com.scrooge.exception.EmailAlreadyExistException;
import com.scrooge.exception.InvalidUserEmailException;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.model.enums.Country;
import com.scrooge.model.enums.Role;
import com.scrooge.repository.UserRepository;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.web.dto.NotificationPreferenceResponse;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
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

        String email = "main.admin@scrooge.com";

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

        Exception exception = assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));

        String expectedMessage = String.format(INVALID_USER_ID, userId);
        assertEquals(expectedMessage, exception.getMessage());
    }

    @Test
    void whenRepositoryReturnEmptyOptional_thenThrowException() {

        UUID userId = UUID.randomUUID();

        when(userRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.getUserById(userId));
    }

    @Test
    void whenUpdateUser_thenSuccessfullyUpdateUser() {

        UUID uuid = UUID.randomUUID();

        User user = User.builder().build();

        UserUpdateRequest request = UserUpdateRequest.builder()
                .firstName("Admin")
                .lastName("Admin")
                .phoneNumber("123123")
                .country(String.valueOf(Country.BULGARIA))
                .build();

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setPhoneNumber(request.getPhoneNumber());

        when(userRepository.findById(uuid)).thenReturn(Optional.of(user));

        userService.update(uuid, request);

        assertEquals(user.getFirstName(), request.getFirstName());
        assertEquals(user.getLastName(), request.getLastName());

        String logMessage = String.format("User with email %s updated.", user.getEmail());
        verify(auditLogService, times(1)).log("UPDATE_USER", logMessage, user);
    }


    @Test
    void givenMissingUserFromDatabase_whenLoadUserByEmail_thenThrowException() {

        String email = "main.admin@test.eu";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(InvalidUserEmailException.class, () -> userService.loadUserByUsername(email));
    }

    @Test
    void whenLoadUserByEmail_thenReturnUserDetails() {

        String email = "main.admin@test.eu";

        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .email(email)
                .role(Role.USER)
                .password("123123")
                .build();

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails userDetails = userService.loadUserByUsername(email);

        assertInstanceOf(CurrentPrinciple.class, userDetails);
        assertEquals(user.getPassword(), userDetails.getPassword());
    }

    @Test
    void whenRegisterUser_thenThrowEmailAlreadyExistException() {

        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(new User()));

        assertThrows(EmailAlreadyExistException.class, () -> userService.register(request));
    }

    @Test
    void givenHappyPath_whenRegister() {

        UserCreateRequest request = UserCreateRequest.builder()
                .email("test@test.bg")
                .password("123123")
                .confirmPassword("123123")
                .build();

        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email(request.getEmail())
                .build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.empty());
        when(userRepository.save(any())).thenReturn(user);
        when(emailNotification.sendNotification(any())).thenReturn(ResponseEntity.ok(null));

        userService.register(request);

        verify(auditLogService, times(2)).log(any(), any(), any());
    }

    @Test
    void whenUserChangeStatusToInactive_thenSuccessfullyChangeStatus() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.switchStatus(user.getId());

        user.setActive(false);
        assertFalse(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void whenUserChangeStatusToActive_thenSuccessfullyChangeStatus() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .active(false)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.switchStatus(user.getId());

        user.setActive(true);
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void whenSwitchRole_thenHappyPath() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .role(Role.USER)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.switchRole(user.getId());

        user.setRole(Role.ADMINISTRATOR);
        assertEquals(user.getRole(), Role.ADMINISTRATOR);
        verify(userRepository, times(1)).save(user);

    }
    @Test
    void whenSwitchRoleToUser_thenHappyPath() {

        User user = User.builder()
                .id(UUID.randomUUID())
                .active(true)
                .role(Role.ADMINISTRATOR)
                .build();

        when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

        userService.switchRole(user.getId());

        user.setRole(Role.USER);
        assertTrue(user.isActive());
        verify(userRepository, times(1)).save(user);

    }

    @Test
    void whenSwitchNotificationPreference_thenHappyPath() {

        UUID id = UUID.randomUUID();
        NotificationPreferenceResponse response = NotificationPreferenceResponse.builder()
                .id(id)
                .enableNotification(true)
                .email("test@test.com")
                .build();

        when(emailNotification.switchNotificationPreference(id)).thenReturn(response);

        userService.switchNotificationPreference(id);

        verify(emailNotification, times(1)).switchNotificationPreference(id);
    }

    @Test
    void whenGetNotificationPreference_thenHappyPath() {

        UUID id = UUID.randomUUID();
        NotificationPreferenceResponse response = NotificationPreferenceResponse.builder()
                .id(id)
                .enableNotification(true)
                .email("test@test.com")
                .build();

        when(emailNotification.getNotificationPreference(id)).thenReturn(response);

        userService.getNotificationPreference(id);

        verify(emailNotification, times(1)).getNotificationPreference(id);
    }
}

