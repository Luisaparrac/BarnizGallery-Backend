package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.PositiveOrZero;

/**
 * Sale data of any artwork set by an administrator; null fields stay as they are.
 */
public record AdminArtworkUpdateRequest(
        @PositiveOrZero BigDecimal price,
        @Pattern(regexp = "[A-Z]{3}") String currency,
        @PositiveOrZero Integer stock,
        SaleMode saleMode,
        ArtworkStatus status) {
}
