package com.barnizgallery.backend.mapper;

import com.barnizgallery.backend.dto.response.ThreeDModelResponse;
import com.barnizgallery.backend.model.entity.ThreeDModel;

/**
 * Converts {@link ThreeDModel} to its DTO.
 */
public final class ThreeDModelMapper {

    private ThreeDModelMapper() {
    }

    public static ThreeDModelResponse toResponse(ThreeDModel model) {
        return new ThreeDModelResponse(model.getModelId(), model.getArtwork().getArtworkId(),
                model.getHyper3dTaskId(), model.getGenerationStatus(), model.getGlbFileUrl(),
                model.getGenerationDate());
    }
}
