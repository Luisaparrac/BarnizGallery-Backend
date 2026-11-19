package com.barnizgallery.backend.dto;

import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Body of {@code POST /v1/pricing/suggest}.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiPricingRequest(
        AiPricingPiece piece,
        List<AiComparable> comparables,
        String currency) {
}
