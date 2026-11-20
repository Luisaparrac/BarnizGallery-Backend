package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtisanVerificationResponse;
import com.barnizgallery.backend.dto.VerificationStepResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.MasterVerification;

/**
 * Converts the verification entities to their DTOs.
 */
public final class VerificationMapper {

    private VerificationMapper() {
    }

    public static ArtisanVerificationResponse toResponse(Master master, List<MasterVerification> steps) {
        return new ArtisanVerificationResponse(master.getMasterId(), master.getName(), master.getVerificationStatus(),
                steps.stream().map(VerificationMapper::toResponse).toList());
    }

    public static VerificationStepResponse toResponse(MasterVerification step) {
        return new VerificationStepResponse(step.getVerificationId(), step.getStep(), step.getStatus(),
                step.getEvidenceUrl(), step.getNotes(), step.getReviewedAt());
    }
}
