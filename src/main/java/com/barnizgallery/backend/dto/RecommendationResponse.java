package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A room recommended to a visitor (score from 0 to 100).
 */
public record RecommendationResponse(
        Long recommendationId,
        Long visitorId,
        Long roomId,
        String roomNameEs,
        String roomNameEn,
        BigDecimal score,
        String reason,
        LocalDateTime recommendationDate) {
}
