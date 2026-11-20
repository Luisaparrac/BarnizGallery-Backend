package com.barnizgallery.backend.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.dto.ReportRequest;
import com.barnizgallery.backend.dto.ReportResponse;
import com.barnizgallery.backend.entity.ContentReport;
import com.barnizgallery.backend.enums.ReportStatus;
import com.barnizgallery.backend.repository.ContentReportRepository;
import com.barnizgallery.backend.repository.UserRepository;
import com.barnizgallery.backend.security.CurrentUser;

/**
 * Logged-in users report content (artworks, stores, messages, custom requests or users) for the administrators.
 */
@Service
public class ContentReportService {

    private final ContentReportRepository reportRepository;
    private final UserRepository userRepository;
    private final CurrentUser currentUser;

    public ContentReportService(ContentReportRepository reportRepository, UserRepository userRepository,
            CurrentUser currentUser) {
        this.reportRepository = reportRepository;
        this.userRepository = userRepository;
        this.currentUser = currentUser;
    }

    @Transactional
    public ReportResponse report(ReportRequest request) {
        ContentReport report = new ContentReport();
        report.setReporter(userRepository.getReferenceById(currentUser.requireId()));
        report.setTargetType(request.entityType());
        report.setTargetId(String.valueOf(request.entityId()));
        report.setReason(request.reason().strip());
        report.setStatus(ReportStatus.PENDING);
        reportRepository.save(report);
        return new ReportResponse(report.getReportId(), request.entityType(), request.entityId(), report.getStatus());
    }
}
