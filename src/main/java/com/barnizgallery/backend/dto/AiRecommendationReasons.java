package com.barnizgallery.backend.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Why an artwork was recommended; {@code budget} is null when it cannot be compared.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiRecommendationReasons(
        List<String> colors,
        boolean shape,
        boolean style,
        List<String> motifs,
        Boolean budget) {
}
