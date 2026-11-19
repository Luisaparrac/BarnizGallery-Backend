package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

/**
 * A master with its room (room is null if it has none yet).
 */
public record MasterDetailResponse(
        Long masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo,
        MasterVerificationStatus verificationStatus,
        RoomSummaryResponse room) {
}
