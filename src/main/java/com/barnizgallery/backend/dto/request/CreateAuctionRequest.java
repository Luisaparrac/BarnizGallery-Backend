package com.barnizgallery.backend.dto.request;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Body to create an auction. basePrice is optional when AI can suggest one.
 */
public record CreateAuctionRequest(
        @NotNull Integer artworkId,
        @PositiveOrZero BigDecimal basePrice,
        @NotNull LocalDateTime startDate,
        @NotNull LocalDateTime endDate) {
}
