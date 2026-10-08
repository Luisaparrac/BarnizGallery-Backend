package com.barnizgallery.backend.dto;

import java.time.LocalDateTime;

import com.barnizgallery.backend.enums.GenerationStatus;

/**
 * The 3D model of an artwork.
 */
public record ThreeDModelResponse(
        Integer modelId,
        Integer artworkId,
        String hyper3dTaskId,
        GenerationStatus generationStatus,
        String glbFileUrl,
        LocalDateTime generationDate) {
}
