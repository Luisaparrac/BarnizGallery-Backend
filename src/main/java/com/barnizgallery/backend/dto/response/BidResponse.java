package com.barnizgallery.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * A bid of an auction.
 */
public record BidResponse(
        Integer bidId,
        Integer auctionId,
        Integer visitorId,
        String visitorName,
        BigDecimal amount,
        String currency,
        LocalDateTime bidDate) {
}
