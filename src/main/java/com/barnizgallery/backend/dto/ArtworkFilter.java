package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

/**
 * Filters of the public gallery (query parameters, all optional). Catalog values are codes; a price range needs
 * its currency. {@code page} starts at 0 and {@code size} is 24 by default (100 at most).
 */
public record ArtworkFilter(
        @Size(max = 60) String color,
        @Size(max = 60) String motif,
        @Size(max = 60) String shape,
        @Size(max = 60) String style,
        @Pattern(regexp = "[A-Z]{3}") String currency,
        @PositiveOrZero BigDecimal minPrice,
        @PositiveOrZero BigDecimal maxPrice,
        @Size(max = 80) String store,
        @Min(0) Integer page,
        @Min(1) @Max(100) Integer size) {
}
