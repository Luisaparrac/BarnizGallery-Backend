package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.VerificationStepStatus;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * The administrator's decision on one verification step: {@code APPROVED} or {@code REJECTED}, with notes the
 * artisan will read (for example what to send again).
 */
public record StepReviewRequest(
        @NotNull VerificationStepStatus decision,
        @Size(max = 1000) String notes) {
}
