package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

/**
 * Base price suggested by the AI for an artwork.
 */
public record SuggestedPriceResponse(
        Long artworkId,
        BigDecimal suggestedPrice,
        String currency) {
}
