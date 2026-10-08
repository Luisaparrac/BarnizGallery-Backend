package com.barnizgallery.backend.dto.response;

import java.math.BigDecimal;

/**
 * Base price suggested by the AI for an artwork.
 */
public record SuggestedPriceResponse(
        Integer artworkId,
        BigDecimal suggestedPrice,
        String currency) {
}
