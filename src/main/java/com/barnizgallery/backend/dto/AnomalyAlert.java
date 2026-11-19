package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Suspicious bid alert sent to {@code /topic/admin/anomalies}. The bid was accepted.
 */
public record AnomalyAlert(Long auctionId, Long bidId, Long userId, BigDecimal amount, String reason,
        LocalDateTime timestamp) {
}
