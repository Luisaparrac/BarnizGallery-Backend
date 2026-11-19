package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.AiUsageLog;

/**
 * Spring Data repository for {@link AiUsageLog}.
 */
public interface AiUsageLogRepository extends JpaRepository<AiUsageLog, Long> {
}
