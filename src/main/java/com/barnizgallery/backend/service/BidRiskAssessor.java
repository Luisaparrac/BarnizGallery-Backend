package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.dto.AiBidAssessRequest;
import com.barnizgallery.backend.dto.AiBidAssessment;
import com.barnizgallery.backend.dto.AiBidAuction;
import com.barnizgallery.backend.dto.AiBidHistoryItem;
import com.barnizgallery.backend.dto.AiBidPlaced;
import com.barnizgallery.backend.dto.AiBidder;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Bid;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.facade.BidAssessment;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * Asks the AI service ({@code POST /v1/bids/assess}) about a bid before it is saved. It can only mark the bid as
 * suspicious, never reject it; when the service is off or fails, the bid is accepted as OK. The service was
 * trained with synthetic data, so the mark is a hint for the administrator, not a sanction.
 */
@Component
public class BidRiskAssessor {

    private static final Logger log = LoggerFactory.getLogger(BidRiskAssessor.class);

    private final AiServiceGateway aiService;
    private final BidRepository bidRepository;
    private final Clock clock;

    public BidRiskAssessor(AiServiceGateway aiService, BidRepository bidRepository, Clock clock) {
        this.aiService = aiService;
        this.bidRepository = bidRepository;
        this.clock = clock;
    }

    /** OK, or SUSPICIOUS with the reasons of the service joined in one text. */
    public BidAssessment assess(Auction auction, BigDecimal amount, User bidder, BigDecimal minIncrement) {
        if (!aiService.isAvailable()) {
            return BidAssessment.ok();
        }
        try {
            AiBidAssessment answer = aiService.assessBid(request(auction, amount, bidder, minIncrement));
            if (answer.isSuspicious()) {
                String reasons = answer.reasons() == null || answer.reasons().isEmpty() ? "AI: SUSPICIOUS"
                        : "AI: " + String.join("; ", answer.reasons());
                return BidAssessment.suspicious(reasons);
            }
        } catch (AiServiceException ex) {
            log.warn("AI bid assessment failed for auction {}: {}", auction.getAuctionId(), ex.getCode());
        }
        return BidAssessment.ok();
    }

    AiBidAssessRequest request(Auction auction, BigDecimal amount, User bidder, BigDecimal minIncrement) {
        List<Bid> bids = new ArrayList<>(bidRepository.findByAuctionWithUser(auction.getAuctionId()));
        bids.sort(Comparator.comparing(Bid::getBidDate, Comparator.nullsFirst(Comparator.naturalOrder())));
        List<AiBidHistoryItem> own = new ArrayList<>();
        Bid leader = null;
        for (Bid bid : bids) {
            boolean mine = bid.getUser().getUserId().equals(bidder.getUserId());
            if (mine) {
                boolean leading = leader != null && leader.getUser().getUserId().equals(bidder.getUserId());
                own.add(new AiBidHistoryItem(bid.getAmount(), instant(bid.getBidDate()), leading));
            }
            if (leader == null || bid.getAmount().compareTo(leader.getAmount()) > 0) {
                leader = bid;
            }
        }
        Bid last = bids.isEmpty() ? null : bids.get(bids.size() - 1);
        AiBidAuction auctionPart = new AiBidAuction(auction.getAuctionId(), auction.getBasePrice(),
                leader == null ? null : leader.getAmount(), leader == null ? null : leader.getUser().getUserId(),
                minIncrement, auction.getCurrency(), instant(auction.getEndDate()),
                last == null ? null : instant(last.getBidDate()));
        return new AiBidAssessRequest(auctionPart, new AiBidPlaced(bidder.getUserId(), amount, clock.instant()),
                new AiBidder(bidder.getCreatedAt(), own));
    }

    private Instant instant(LocalDateTime time) {
        return time == null ? null : time.atZone(clock.getZone()).toInstant();
    }
}
