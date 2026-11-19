package com.barnizgallery.backend.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.barnizgallery.backend.entity.ContentReport;

/**
 * Spring Data repository for {@link ContentReport}.
 */
public interface ContentReportRepository extends JpaRepository<ContentReport, Long> {
}
