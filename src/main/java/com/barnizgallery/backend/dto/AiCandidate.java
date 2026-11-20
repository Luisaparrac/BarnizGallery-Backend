package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * An available artwork the AI service can rank. {@code slug} lets the service match it with what it learned
 * from the museum's real pieces.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiCandidate(
        Long artworkId,
        String slug,
        Long storeId,
        List<String> colors,
        String shape,
        String style,
        List<String> motifs,
        BigDecimal price,
        String currency,
        long popularity) {
}
