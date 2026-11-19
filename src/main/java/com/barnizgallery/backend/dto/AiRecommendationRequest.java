package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Body of {@code POST /v1/recommendations}.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationRequest(
        AiTasteProfile profile,
        List<AiInteraction> interactions,
        List<AiCandidate> candidates,
        int limit) {
}
