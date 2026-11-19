package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Taste profile sent to the AI service, as catalog codes.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiTasteProfile(
        List<String> colors,
        List<String> shapes,
        List<String> styles,
        List<String> motifs,
        AiBudget budget) {
}
