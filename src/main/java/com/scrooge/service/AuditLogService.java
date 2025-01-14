package com.scrooge.service;

import com.scrooge.model.AuditLog;
import com.scrooge.model.User;
import com.scrooge.web.mapper.AuditLogMapper;
import com.scrooge.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;

    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    public AuditLog log(String actionType, String message, User user) {

        AuditLog auditLog = AuditLogMapper.mapToAuditLog(message, actionType);
        auditLog.setUser(user);

        return auditLogRepository.save(auditLog);
    }
}
