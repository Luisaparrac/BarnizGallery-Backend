package com.barnizgallery.backend.mapper;

import java.math.BigDecimal;

import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.dto.BidResponse;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Bid;

/**
 * Converts {@link Auction} and {@link Bid} to their DTOs.
 */
public final class AuctionMapper {

    private AuctionMapper() {
    }

    /**
     * @param highestBid highest bid amount, or null if there are no bids
     * @param bidCount   number of bids
     */
    public static AuctionResponse toResponse(Auction auction, BigDecimal highestBid, long bidCount) {
        return new AuctionResponse(auction.getAuctionId(), auction.getArtwork().getArtworkId(),
                auction.getArtwork().getTitleEs(), auction.getArtwork().getTitleEn(), auction.getBasePrice(),
                auction.getCurrency(), auction.getStartDate(), auction.getEndDate(), auction.getStatus(), highestBid, bidCount);
    }

    public static BidResponse toResponse(Bid bid) {
        return new BidResponse(bid.getBidId(), bid.getAuction().getAuctionId(), bid.getUser().getUserId(),
                bid.getUser().getName(), bid.getAmount(), bid.getCurrency(), bid.getBidDate());
    }
}
