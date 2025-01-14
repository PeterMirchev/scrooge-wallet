package com.scrooge.web.mapper;

import com.scrooge.model.AuditLog;
import com.scrooge.model.User;

import java.time.LocalDateTime;

public class AuditLogMapper {

    public static AuditLog mapToAuditLog(User user, String message, String action) {

        return AuditLog.builder()
                .message(message)
                .action(action)
                .createdOn(LocalDateTime.now())
                .user(user)
                .build();
    }
}
