package com.barnizgallery.backend.patterns.observer;

/**
 * Kinds of {@link AuctionEvent} published by the {@link AuctionEventPublisher}.
 */
public enum AuctionEventType {
    BID_PLACED, AUCTION_STARTED, AUCTION_FINISHED, AUCTION_CANCELLED
}
