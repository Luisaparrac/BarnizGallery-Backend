package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * One ranked artwork with a score from 0 to 1 and, since service 1.1, a short Spanish {@code explanation}.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationItem(
        Long artworkId,
        Long storeId,
        double score,
        AiRecommendationReasons reasons,
        String explanation) {
}
