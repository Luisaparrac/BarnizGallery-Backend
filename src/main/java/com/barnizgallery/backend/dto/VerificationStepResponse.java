package com.barnizgallery.backend.dto;

import java.time.Instant;

import com.barnizgallery.backend.enums.VerificationStep;
import com.barnizgallery.backend.enums.VerificationStepStatus;

/**
 * One verification step of a master artisan; {@code notes} are the administrator's comments.
 */
public record VerificationStepResponse(
        Long verificationId,
        VerificationStep step,
        VerificationStepStatus status,
        String evidenceUrl,
        String notes,
        Instant reviewedAt) {
}
