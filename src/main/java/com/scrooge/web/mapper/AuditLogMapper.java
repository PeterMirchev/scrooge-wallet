package com.scrooge.web.mapper;

import com.scrooge.model.AuditLog;

import java.time.LocalDateTime;

public class AuditLogMapper {

    public static AuditLog mapToAuditLog(String message, String action) {

        return AuditLog.builder()
                .message(message)
                .action(action)
                .createdOn(LocalDateTime.now())
                .build();
    }
}
