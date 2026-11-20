package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

/**
 * A master as the frontend reads it ({@code Master} of its domain).
 */
public record MasterDataResponse(Long id, Long userId, String name, String slug, String city, String workshop,
        String biographyEs, String biographyEn, String profilePhotoUrl, MasterVerificationStatus verificationStatus,
        Instant verifiedAt) {
}
