package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

/**
 * An artwork of the public gallery that looks like the one being viewed. {@code score} goes from 0 to 100;
 * {@code source} is {@code ai} when the AI service ranked it and {@code rules} for the fixed rules.
 */
public record SimilarArtworkResponse(
        Long artworkId,
        Long storeId,
        String titleEs,
        String titleEn,
        BigDecimal price,
        String currency,
        double score,
        String reason,
        String source) {
}
