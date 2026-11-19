package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/**
 * Budget of a taste profile; only compared with artworks in the same currency.
 */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiBudget(
        BigDecimal min,
        BigDecimal max,
        String currency) {
}
