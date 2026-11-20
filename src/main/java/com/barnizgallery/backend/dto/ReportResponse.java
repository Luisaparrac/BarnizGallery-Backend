package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.ReportStatus;

/**
 * A content report waiting for the administrators.
 */
public record ReportResponse(Long reportId, String entityType, Long entityId, ReportStatus status) {
}
