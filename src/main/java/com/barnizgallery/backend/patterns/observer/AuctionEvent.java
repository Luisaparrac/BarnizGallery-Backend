package com.barnizgallery.backend.patterns.observer;

import java.time.LocalDateTime;

import com.barnizgallery.backend.dto.BidResponse;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Message sent by the {@link AuctionEventPublisher} to every {@link AuctionObserver}.
 * It is also the JSON sent to {@code /topic/auctions/{auctionId}}.
 *
 * @param bid                 the new bid (only for BID_PLACED)
 * @param previousTopBidderId visitor who had the highest bid before this one (only for BID_PLACED, may be null)
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record AuctionEvent(
        AuctionEventType type,
        Integer auctionId,
        Integer artworkId,
        BidResponse bid,
        Integer previousTopBidderId,
        LocalDateTime timestamp) {

    public static AuctionEvent bidPlaced(Integer auctionId, Integer artworkId, BidResponse bid,
            Integer previousTopBidderId) {
        return new AuctionEvent(AuctionEventType.BID_PLACED, auctionId, artworkId, bid, previousTopBidderId,
                LocalDateTime.now());
    }

    public static AuctionEvent of(AuctionEventType type, Integer auctionId, Integer artworkId) {
        return new AuctionEvent(type, auctionId, artworkId, null, null, LocalDateTime.now());
    }
}
