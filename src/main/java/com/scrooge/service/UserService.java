package com.scrooge.service;

import com.scrooge.exception.ResourceNotFoundException;
import com.scrooge.model.User;
import com.scrooge.security.CurrentPrinciple;
import com.scrooge.web.mapper.UserMapper;
import com.scrooge.repository.UserRepository;
import com.scrooge.web.dto.UserCreateRequest;
import com.scrooge.web.dto.UserUpdateRequest;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import static com.scrooge.exception.ExceptionMessages.INVALID_USER_EMAIL;
import static com.scrooge.exception.ExceptionMessages.INVALID_USER_ID;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final AuditLogService auditLogService;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, AuditLogService auditLogService, PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.auditLogService = auditLogService;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUserByEmail(String email) {

        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException(INVALID_USER_EMAIL.formatted(email)));
    }

    public void register(UserCreateRequest request) {

        User user = UserMapper.mapToUser(request);

        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String logMessage = String.format("User with email %s created.", request.getEmail());
        auditLogService.log("USER_CREATED", logMessage, user);

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
}
