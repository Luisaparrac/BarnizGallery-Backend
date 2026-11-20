package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

/**
 * A master with its store (store is null if it has none yet).
 */
public record MasterDetailResponse(
        Long masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo,
        MasterVerificationStatus verificationStatus,
        boolean active,
        StoreSummaryResponse store) {
}
