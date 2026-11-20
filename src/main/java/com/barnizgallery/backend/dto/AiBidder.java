package com.barnizgallery.backend.dto;

import java.time.Instant;
import java.util.List;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** The bidder sent to {@code POST /v1/bids/assess}: account age and earlier bids in this auction. */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiBidder(Instant accountCreatedAt, List<AiBidHistoryItem> bids) {
}
