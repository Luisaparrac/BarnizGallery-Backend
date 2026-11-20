package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * An artisan's offer ({@code OfferInput} of the frontend). {@code ACCEPT_PRICE} takes the offered price.
 */
public record OfferInput(
        @NotNull @Pattern(regexp = "ACCEPT_PRICE|COUNTER_OFFER") String type,
        @PositiveOrZero BigDecimal price,
        @NotNull @PositiveOrZero @Max(3650) Integer estimatedDays,
        @Size(max = 2000) String message,
        Long designId) {
}
