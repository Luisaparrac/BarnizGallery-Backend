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
                auction.getCurrency(), auction.getStartDate(), auction.getEndDate(), auction.getStatus(), highestBid, bidCount,
                auction.getAuctionId(), auction.getArtwork().getStore().getStoreId(),
                auction.getArtwork().getStore().getNameEs(), null, auction.getReservePrice(), auction.getMinIncrement(),
                null, auction.getWinningBid() == null ? null : auction.getWinningBid().getBidId(),
                auction.getOrder() == null ? null : auction.getOrder().getOrderId());
    }

    public static BidResponse toResponse(Bid bid) {
        return new BidResponse(bid.getBidId(), bid.getAuction().getAuctionId(), bid.getUser().getUserId(),
                maskName(bid.getUser().getName()), bid.getAmount(), bid.getCurrency(), bid.getBidDate(), bid.getBidId(),
                bid.getReviewStatus(), bid.getReviewReason());
    }

    /** Public bid history hides the bidder: "Ana Cabrera" → "A*** C.". */
    static String maskName(String name) {
        if (name == null || name.isBlank()) {
            return "***";
        }
        String[] parts = name.strip().split("\s+");
        String masked = parts[0].charAt(0) + "***";
        return parts.length > 1 ? masked + " " + parts[parts.length - 1].charAt(0) + "." : masked;
    }
}
