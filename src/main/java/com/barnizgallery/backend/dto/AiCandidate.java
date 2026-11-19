package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * An available artwork the AI service can rank.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiCandidate(
        Long artworkId,
        Long storeId,
        List<String> colors,
        String shape,
        String style,
        List<String> motifs,
        BigDecimal price,
        String currency,
        long popularity) {
}
