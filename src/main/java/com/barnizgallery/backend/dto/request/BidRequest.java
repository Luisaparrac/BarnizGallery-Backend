package com.barnizgallery.backend.dto.request;

import java.math.BigDecimal;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Body to place a bid in an auction.
 */
public record BidRequest(
        @NotNull Integer visitorId,
        @NotNull @Positive BigDecimal amount,
        @NotBlank @Size(max = 10) String currency) {
}
