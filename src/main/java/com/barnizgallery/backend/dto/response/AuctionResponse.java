package com.barnizgallery.backend.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.barnizgallery.backend.model.enums.AuctionStatus;

/**
 * An auction with its highest bid (null if none) and number of bids.
 */
public record AuctionResponse(
        Integer auctionId,
        Integer artworkId,
        String artworkTitleEs,
        String artworkTitleEn,
        BigDecimal basePrice,
        String currency,
        LocalDateTime startDate,
        LocalDateTime endDate,
        AuctionStatus status,
        BigDecimal highestBid,
        long bidCount) {
}
