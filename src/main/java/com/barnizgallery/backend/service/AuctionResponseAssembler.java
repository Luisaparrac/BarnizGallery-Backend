package com.barnizgallery.backend.service;

import java.math.BigDecimal;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.config.AppProperties;
import com.barnizgallery.backend.dto.response.AuctionResponse;
import com.barnizgallery.backend.mapper.AuctionMapper;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * Builds {@link AuctionResponse} objects, adding the highest bid, the number of bids
 * and the configured currency. Must be called inside a transaction.
 */
@Component
public class AuctionResponseAssembler {

    private final BidRepository bidRepository;
    private final AppProperties properties;

    public AuctionResponseAssembler(BidRepository bidRepository, AppProperties properties) {
        this.bidRepository = bidRepository;
        this.properties = properties;
    }

    public AuctionResponse toResponse(Auction auction) {
        BigDecimal highest = bidRepository.findHighestAmount(auction.getAuctionId());
        long count = bidRepository.countByAuctionAuctionId(auction.getAuctionId());
        return AuctionMapper.toResponse(auction, properties.auction().currency(), highest, count);
    }
}
