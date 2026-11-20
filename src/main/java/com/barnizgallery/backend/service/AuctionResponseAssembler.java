package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.dto.AuctionResponse;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Photo;
import com.barnizgallery.backend.mapper.AuctionMapper;
import com.barnizgallery.backend.repository.AuctionBidStats;
import com.barnizgallery.backend.repository.BidRepository;
import com.barnizgallery.backend.repository.PhotoRepository;

/**
 * Builds {@link AuctionResponse} objects, adding the highest bid and the number of bids.
 * Must be called inside a transaction.
 */
@Component
public class AuctionResponseAssembler {

    private final BidRepository bidRepository;

    private final PhotoRepository photoRepository;

    public AuctionResponseAssembler(BidRepository bidRepository, PhotoRepository photoRepository) {
        this.bidRepository = bidRepository;
        this.photoRepository = photoRepository;
    }

    public AuctionResponse toResponse(Auction auction) {
        BigDecimal highest = bidRepository.findHighestAmount(auction.getAuctionId());
        long count = bidRepository.countByAuctionAuctionId(auction.getAuctionId());
        return AuctionMapper.toResponse(auction, highest, count,
                covers(List.of(auction)).get(auction.getArtwork().getArtworkId()));
    }

    /** Same as {@link #toResponse(Auction)} for a list, with one query for all the bid stats. */
    public List<AuctionResponse> toResponses(List<Auction> auctions) {
        if (auctions.isEmpty()) {
            return List.of();
        }
        Map<Long, AuctionBidStats> stats = bidRepository
                .findStats(auctions.stream().map(Auction::getAuctionId).toList()).stream()
                .collect(Collectors.toMap(AuctionBidStats::getAuctionId, Function.identity()));
        Map<Long, String> covers = covers(auctions);
        return auctions.stream().map(auction -> {
            AuctionBidStats s = stats.get(auction.getAuctionId());
            return AuctionMapper.toResponse(auction, s == null ? null : s.getHighest(),
                    s == null ? 0 : s.getBidCount(), covers.get(auction.getArtwork().getArtworkId()));
        }).toList();
    }

    /** Cover of each auctioned artwork: its primary photo, or the first one; one query for all. */
    private Map<Long, String> covers(List<Auction> auctions) {
        Map<Long, String> covers = new HashMap<>();
        photoRepository.findByArtworkArtworkIdInOrderBySortOrderAscPhotoIdAsc(
                auctions.stream().map(auction -> auction.getArtwork().getArtworkId()).toList()).stream()
                .sorted(Comparator.comparing(Photo::isPrimary).reversed())
                .forEach(photo -> covers.putIfAbsent(photo.getArtwork().getArtworkId(), photo.getFileUrl()));
        return covers;
    }
}
