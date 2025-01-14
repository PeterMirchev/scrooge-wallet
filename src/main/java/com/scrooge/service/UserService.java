package com.scrooge.service;

import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.web.dto.RequestLogin;
import com.scrooge.web.mapper.UserMapper;
import com.scrooge.repository.UserRepository;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.INVALID_USER_EMAIL;
import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AuditLogService auditLogService, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    public User login(RequestLogin requestLogin) {

        User user = getUserByEmail(requestLogin.getEmail());

        if (user == null) {
            throw new ResourceNotFoundException("Incorrect username or password.");
        }

        if (!passwordEncoder.matches(requestLogin.getPassword(), user.getPassword())) {
            auditLogService.log("LOGIN_FAIL", "Failed login attempt for user: %s".formatted(user.getEmail()), user);
            throw new ResourceNotFoundException("Incorrect username or password.");
        }

        auditLogService.log("LOGIN", "Successfully logged in.", user);
        return user;
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_USER_EMAIL.formatted(email)));
    }
    public User register(UserCreateRequest request) {

        User user = UserMapper.mapToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        User persistedUser = userRepository.save(user);

        String logMessage = String.format("User with email %s created.", request.getEmail());
        auditLogService.log("USER_CREATED", logMessage, user);

        return persistedUser;
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
}
