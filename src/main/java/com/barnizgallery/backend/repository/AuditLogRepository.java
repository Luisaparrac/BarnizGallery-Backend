package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.AuditLog;

/**
 * Spring Data repository for {@link AuditLog}.
 */
public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
}
