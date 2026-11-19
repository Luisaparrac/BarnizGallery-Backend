package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A bid of an auction.
 */
public record BidResponse(
        Long bidId,
        Long auctionId,
        Long visitorId,
        String visitorName,
        BigDecimal amount,
        String currency,
        LocalDateTime bidDate) {
}
