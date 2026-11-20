package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A store or an artwork recommended to a user (score from 0 to 100); the other id is null.
 */
public record RecommendationResponse(
        Long recommendationId,
        Long userId,
        Long artworkId,
        Long storeId,
        String storeNameEs,
        String storeNameEn,
        BigDecimal score,
        String reason,
        LocalDateTime recommendationDate) {
}
