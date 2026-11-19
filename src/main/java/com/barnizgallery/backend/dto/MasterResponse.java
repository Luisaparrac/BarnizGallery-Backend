package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

/**
 * A master artisan.
 */
public record MasterResponse(
        Long masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo,
        MasterVerificationStatus verificationStatus) {
}
