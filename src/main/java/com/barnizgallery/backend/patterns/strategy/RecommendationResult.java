package com.barnizgallery.backend.patterns.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Result produced by every {@link RecommendationStrategy}: a room, a score from 0 to 100
 * and a reason written in the visitor's preferred language.
 */
public record RecommendationResult(Integer roomId, BigDecimal score, String reason) {

    /** Creates a result, clamping the score to 0-100 with 2 decimals (column numeric(5,2)). */
    public static RecommendationResult of(Integer roomId, double score, String reason) {
        double clamped = Math.max(0, Math.min(100, score));
        return new RecommendationResult(roomId, BigDecimal.valueOf(clamped).setScale(2, RoundingMode.HALF_UP),
                reason);
    }
}
