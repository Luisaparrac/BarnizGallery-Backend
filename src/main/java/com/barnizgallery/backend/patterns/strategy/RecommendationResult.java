package com.barnizgallery.backend.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Result produced by every {@link RecommendationStrategy}: a store <b>or</b> an artwork (never both, as the
 * table {@code recommendations} requires), a score from 0 to 100 and a reason written in the user's language.
 */
public record RecommendationResult(Long storeId, Long artworkId, BigDecimal score, String reason) {

    /** A store result, clamping the score to 0-100 with 2 decimals (column numeric(5,2)). */
    public static RecommendationResult of(Long storeId, double score, String reason) {
        return new RecommendationResult(storeId, null, clamp(score), reason);
    }

    /** An artwork result, clamping the score to 0-100 with 2 decimals. */
    public static RecommendationResult ofArtwork(Long artworkId, double score, String reason) {
        return new RecommendationResult(null, artworkId, clamp(score), reason);
    }

    /** The id of what is recommended: the artwork for artwork results, otherwise the store. */
    public Long targetId() {
        return artworkId != null ? artworkId : storeId;
    }

    private static BigDecimal clamp(double score) {
        double clamped = Math.max(0, Math.min(100, score));
        return BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP);
    }
}
