package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.config.AuctionProperties;
import com.barnizgallery.backend.dto.response.AuctionResponse;
import com.barnizgallery.backend.mapper.AuctionMapper;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.repository.BidRepository;
import com.barnizgallery.backend.repository.AuctionBidStats;

/**
 * Builds {@link AuctionResponse} objects, adding the highest bid, the number of bids
 * and the configured currency. Must be called inside a transaction.
 */
@Component
public class AuctionResponseAssembler {

    private final BidRepository bidRepository;
    private final AuctionProperties auctionProperties;

    public AuctionResponseAssembler(BidRepository bidRepository, AuctionProperties auctionProperties) {
        this.bidRepository = bidRepository;
        this.auctionProperties = auctionProperties;
    }

    public AuctionResponse toResponse(Auction auction) {
        BigDecimal highest = bidRepository.findHighestAmount(auction.getAuctionId());
        long count = bidRepository.countByAuctionAuctionId(auction.getAuctionId());
        return AuctionMapper.toResponse(auction, auctionProperties.currency(), highest, count);
    }

    /** Same as {@link #toResponse(Auction)} for a list, with one query for all the bid stats. */
    public List<AuctionResponse> toResponses(List<Auction> auctions) {
        if (auctions.isEmpty()) {
            return List.of();
        }
        Map<Integer, AuctionBidStats> stats = bidRepository
                .findStats(auctions.stream().map(Auction::getAuctionId).toList()).stream()
                .collect(Collectors.toMap(AuctionBidStats::getAuctionId, Function.identity()));
        String currency = auctionProperties.currency();
        return auctions.stream().map(auction -> {
            AuctionBidStats s = stats.get(auction.getAuctionId());
            return AuctionMapper.toResponse(auction, currency, s == null ? null : s.getHighest(),
                    s == null ? 0 : s.getBidCount());
        }).toList();
    }
}
