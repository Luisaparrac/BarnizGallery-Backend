package com.barnizgallery.backend.dto;

import java.util.List;

import com.barnizgallery.backend.enums.MasterVerificationStatus;

/**
 * Verification of a master artisan: the overall status and each step.
 */
public record ArtisanVerificationResponse(
        Long masterId,
        String name,
        MasterVerificationStatus verificationStatus,
        List<VerificationStepResponse> steps) {
}
