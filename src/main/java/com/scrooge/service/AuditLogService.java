package com.scrooge.service;

import com.scrooge.model.AuditLog;
import com.scrooge.model.User;
import com.scrooge.web.mapper.AuditLogMapper;
import com.scrooge.repository.AuditLogRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class AuditLogService {
    private final AuditLogRepository auditLogRepository;
    public AuditLogService(AuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;

    }

    public AuditLog log(String actionType, String message, User user) {

        AuditLog auditLog = AuditLogMapper.mapToAuditLog(user, message, actionType);

        return auditLogRepository.save(auditLog);
    }

    public List<AuditLog> getAuditLogsForUser(UUID userId) {
        return auditLogRepository.findTopNByUserIdOrderByCreatedOnDesc(userId);
    }

    public List<AuditLog> findLogsBefore(LocalDateTime thresholdTime) {

        return  auditLogRepository.findByCreatedOnBefore(thresholdTime);
    }

    public void deleteLogs(List<AuditLog> oldLogs) {

        oldLogs.forEach(auditLogRepository::delete);
    }
}
