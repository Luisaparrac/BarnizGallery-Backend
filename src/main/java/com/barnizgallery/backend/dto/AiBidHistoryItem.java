package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** An earlier bid of the same bidder in the same auction; {@code raisedOwn} = placed while already leading. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiBidHistoryItem(BigDecimal amount, Instant placedAt, boolean raisedOwn) {
}
