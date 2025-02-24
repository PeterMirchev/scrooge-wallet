package com.scrooge.repository;

import com.scrooge.model.AuditLog;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    List<AuditLog> findTopNByUserIdOrderByCreatedOnDesc(UUID userId, PageRequest pageRequest);
}
