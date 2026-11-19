package com.barnizgallery.backend.dto;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * An interaction of the user with one artwork ({@code VIEW} or {@code ZOOM} today).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiInteraction(
        Long artworkId,
        String action,
        Integer durationSeconds) {
}
