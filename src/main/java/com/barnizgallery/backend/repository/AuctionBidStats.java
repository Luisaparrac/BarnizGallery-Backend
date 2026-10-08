package com.barnizgallery.backend.repository;

import java.math.BigDecimal;

/**
 * Projection: highest bid and number of bids of one auction ({@link BidRepository#findStats}).
 */
public interface AuctionBidStats {

    Integer getAuctionId();

    BigDecimal getHighest();

    long getBidCount();
}
