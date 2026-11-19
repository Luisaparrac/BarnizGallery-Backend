package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * The piece to price, described with catalog codes (shape is required by the AI service).
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiPricingPiece(
        String shape,
        String style,
        List<String> colors,
        List<String> motifs,
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal depthCm,
        String technique) {
}
