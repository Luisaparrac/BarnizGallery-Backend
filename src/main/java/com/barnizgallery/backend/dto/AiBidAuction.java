package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

import tools.jackson.databind.PropertyNamingStrategies;
import tools.jackson.databind.annotation.JsonNaming;

/** The auction part of {@code POST /v1/bids/assess}: prices, current leader and times (with time zone). */
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
public record AiBidAuction(
        long auctionId,
        BigDecimal basePrice,
        BigDecimal currentBid,
        Long leaderBidderId,
        BigDecimal minIncrement,
        String currency,
        Instant endsAt,
        Instant lastBidAt) {
}
