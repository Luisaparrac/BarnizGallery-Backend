package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A store recommended to a user (score from 0 to 100).
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
