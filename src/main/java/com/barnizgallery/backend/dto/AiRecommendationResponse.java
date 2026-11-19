package com.barnizgallery.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Answer of {@code POST /v1/recommendations}: strategy used and artworks, best first.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationResponse(
        String strategy,
        List<AiRecommendationItem> items) {
}
