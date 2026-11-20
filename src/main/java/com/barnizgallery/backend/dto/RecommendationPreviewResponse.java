package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;

/**
 * A recommendation computed for a visitor without an account; it is not saved, so {@code id} is null.
 */
public record RecommendationPreviewResponse(Long id, Long storeId, Long artworkId, BigDecimal score, String reason,
        String strategy, Instant createdAt) {
}
