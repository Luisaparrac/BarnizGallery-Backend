package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.barnizgallery.backend.enums.BidReviewStatus;

/**
 * A bid of an auction.
 */
public record BidResponse(
        Long bidId,
        Long auctionId,
        Long userId,
        String userName,
        BigDecimal amount,
        String currency,
        LocalDateTime bidDate,
        Long id,
        BidReviewStatus reviewStatus,
        String reviewReason) {
}
