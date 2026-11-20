package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** The new bid sent to {@code POST /v1/bids/assess}. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiBidPlaced(long bidderId, BigDecimal amount, Instant placedAt) {
}
