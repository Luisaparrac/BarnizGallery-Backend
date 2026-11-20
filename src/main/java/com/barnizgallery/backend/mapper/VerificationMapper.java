package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.entity.MasterVerification;

/**
 * Converts the verification entities to their DTOs.
 */
public final class VerificationMapper {

    private VerificationMapper() {
    }

    public static VerificationStepResponse toResponse(MasterVerification step) {
        return new VerificationStepResponse(step.getVerificationId(), step.getStep(), step.getStatus(),
                step.getEvidenceUrl(), step.getNotes(), step.getReviewedAt());
    }
}
