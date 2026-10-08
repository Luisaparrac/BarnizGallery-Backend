package com.barnizgallery.backend.mapper;

import java.math.BigDecimal;

import com.barnizgallery.backend.dto.response.AuctionResponse;
import com.barnizgallery.backend.dto.response.BidResponse;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.entity.Bid;

/**
 * Converts {@link Auction} and {@link Bid} to their DTOs.
 */
public final class AuctionMapper {

    private AuctionMapper() {
    }

    /**
     * @param currency   currency of the auction (single currency configured in the app)
     * @param highestBid highest bid amount, or null if there are no bids
     * @param bidCount   number of bids
     */
    public static AuctionResponse toResponse(Auction auction, String currency, BigDecimal highestBid,
            long bidCount) {
        return new AuctionResponse(auction.getAuctionId(), auction.getArtwork().getArtworkId(),
                auction.getArtwork().getTitleEs(), auction.getArtwork().getTitleEn(), auction.getBasePrice(),
                currency, auction.getStartDate(), auction.getEndDate(), auction.getStatus(), highestBid, bidCount);
    }

    public static BidResponse toResponse(Bid bid) {
        return new BidResponse(bid.getBidId(), bid.getAuction().getAuctionId(), bid.getVisitor().getVisitorId(),
                bid.getVisitor().getName(), bid.getAmount(), bid.getCurrency(), bid.getBidDate());
    }
}
