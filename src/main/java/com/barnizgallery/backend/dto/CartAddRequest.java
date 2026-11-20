package com.barnizgallery.backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * Adds units of an artwork to the cart; without {@code quantity} one unit is added.
 */
public record CartAddRequest(@NotNull Long artworkId, @Min(1) @Max(99) Integer quantity) {
}
