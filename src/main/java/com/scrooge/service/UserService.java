package com.scrooge.service;

import com.scrooge.config.client.EmailNotification;
import com.scrooge.exception.EmailAlreadyExistException;
import com.scrooge.exception.NotificationException;
import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.AuditLog;
import com.scrooge.model.User;
import com.scrooge.model.enums.NotificationType;
import com.scrooge.model.enums.Role;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.web.dto.NotificationPreferenceCreateRequest;
import com.scrooge.web.dto.NotificationRequest;
import com.scrooge.web.mapper.UserMapper;
import com.scrooge.repository.UserRepository;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;
import feign.RetryableException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.scrooge.config.CommonMessages.REGISTRATION_MESSAGE;
import static com.scrooge.config.CommonMessages.WELCOME_MESSAGE;
import static com.scrooge.exception.ExceptionMessages.INVALID_USER_EMAIL;
import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;
    private final EmailNotification emailNotification;

    public UserService(UserRepository userRepository, AuditLogService auditLogService, PasswordEncoder passwordEncoder, EmailNotification emailNotification) {

        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
        this.emailNotification = emailNotification;
    }

    public Optional<User> getRootAdmin(String email) {

        return userRepository.findByEmail(email);
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_USER_EMAIL.formatted(email)));
    }

    public void register(UserCreateRequest request) {

        Optional<User> optionalUser = userRepository.findByEmail(request.getEmail());

        if (optionalUser.isPresent()) {
            throw new EmailAlreadyExistException("Email already registered - [%s]".formatted(request.getEmail()));
        }

        User user = UserMapper.mapToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        User persistedUser = userRepository.save(user);

        String logMessage = String.format("User with email %s created.", request.getEmail());
        auditLogService.log("USER_CREATED", logMessage, persistedUser);

        NotificationRequest notificationRequest = mapNotificationRequest(user, persistedUser);

        NotificationPreferenceCreateRequest notificationPreferenceCreateRequest = mapNotificationPreferenceCreateRequest(user);

        emailNotification.createNotificationPreference(notificationPreferenceCreateRequest);
        try {
            emailNotification.sendNotification(notificationRequest);
        } catch (RetryableException e) {
            throw new NotificationException(e.getClass().getSimpleName());
        }
    }

    public User getUserById(UUID userId) {

        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_USER_ID.formatted(userId)));
    }

    public List<User> getAllUsers() {

        return userRepository.findAll();
    }

    public User update(UUID userId, UserUpdateRequest request) {

        User user = getUserById(userId);

        UserMapper.mapUserUpdateToUser(user, request);

        String logMessage = String.format("User with email %s updated.", user.getEmail());
        auditLogService.log("UPDATE_USER", logMessage, user);

        return userRepository.save(user);
    }

    public User save(User user) {

        return userRepository.save(user);
    }

    public void deleteById(UUID userId) {

        userRepository.findById(userId)
                .ifPresentOrElse(
                        userRepository::delete, () -> {
                            throw new ResourceNotFoundException(INVALID_USER_ID.formatted(userId));
                        });
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {

        User user = getUserByEmail(email);

        return new CurrentPrinciple(user.getId(), email, user.getPassword(), user.getRole());
    }

    public void switchStatus(UUID userId) {

        User user = getUserById(userId);

        user.setActive(!user.isActive());

        userRepository.save(user);

        String message = "Your account has been deactivated by admin user.";
        auditLogService.log("ACCOUNT_DEACTIVATION", message, user);
    }

    public void switchRole(UUID userId) {

        User user = getUserById(userId);

        if (user.getRole() == Role.USER) {
            user.setRole(Role.ADMINISTRATOR);
        } else {
            user.setRole(Role.USER);
        }

        userRepository.save(user);
        String message = "Your account role has been set to %s by admin user.".formatted(user.getRole());
        auditLogService.log("ROLE_MODIFICATION", message, user);
    }

    protected static NotificationPreferenceCreateRequest mapNotificationPreferenceCreateRequest(User user) {

        return NotificationPreferenceCreateRequest
                .builder()
                .userId(user.getId())
                .enableNotification(true)
                .email(user.getEmail())
                .build();
    }

    protected static NotificationRequest mapNotificationRequest(User user, User persistedUser) {

        return NotificationRequest.builder()
                .userId(persistedUser.getId())
                .subject(REGISTRATION_MESSAGE)
                .body(WELCOME_MESSAGE.formatted(user.getFirstName()))
                .email(persistedUser.getEmail())
                .type(NotificationType.NOTIFICATION)
                .build();
    }

    public void removeAuditLogFromUser(AuditLog log) {

        User user = log.getUser();
        user.getAuditLog().remove(log);
        userRepository.save(user);
    }
}
