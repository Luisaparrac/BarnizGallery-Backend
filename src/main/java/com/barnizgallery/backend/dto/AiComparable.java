package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * A priced artwork the AI service compares with the piece. Only those in the requested currency count.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiComparable(
        Long artworkId,
        String shape,
        String style,
        List<String> colors,
        List<String> motifs,
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal depthCm,
        BigDecimal price,
        String currency,
        boolean sold) {
}
