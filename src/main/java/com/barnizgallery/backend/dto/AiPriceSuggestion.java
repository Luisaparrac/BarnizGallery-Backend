package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Price suggested by the AI service (a suggestion: the artisan decides).
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiPriceSuggestion(
        BigDecimal suggestedPrice,
        BigDecimal minPrice,
        BigDecimal maxPrice,
        String currency,
        String method,
        String explanation) {
}
