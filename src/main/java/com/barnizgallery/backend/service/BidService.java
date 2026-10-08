package com.barnizgallery.backend.service;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.barnizgallery.backend.config.AuctionProperties;
import com.barnizgallery.backend.dto.AnomalyAlert;
import com.barnizgallery.backend.dto.BidRequest;
import com.barnizgallery.backend.dto.BidResponse;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.Bid;
import com.barnizgallery.backend.entity.Visitor;
import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.mapper.AuctionMapper;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.patterns.facade.BidAssessment;
import com.barnizgallery.backend.patterns.facade.BidVerdict;
import com.barnizgallery.backend.patterns.observer.AuctionEvent;
import com.barnizgallery.backend.patterns.observer.AuctionEventPublisher;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * Live bidding. The State pattern decides whether the auction accepts bids, the AI Facade
 * reviews the bid, and the Observer pattern notifies everyone after it is saved.
 */
@Service
public class BidService {

    private static final Logger log = LoggerFactory.getLogger(BidService.class);
    static final String ANOMALIES_TOPIC = "/topic/admin/anomalies";

    private final BidRepository bidRepository;
    private final AuctionService auctionService;
    private final VisitorService visitorService;
    private final AiFacade aiFacade;
    private final AuctionEventPublisher eventPublisher;
    private final SimpMessageSendingOperations messaging;
    private final AuctionProperties auctionProperties;
    private final Clock clock;

    public BidService(BidRepository bidRepository, AuctionService auctionService, VisitorService visitorService,
            AiFacade aiFacade, AuctionEventPublisher eventPublisher, SimpMessageSendingOperations messaging,
            AuctionProperties auctionProperties, Clock clock) {
        this.bidRepository = bidRepository;
        this.auctionService = auctionService;
        this.visitorService = visitorService;
        this.aiFacade = aiFacade;
        this.eventPublisher = eventPublisher;
        this.messaging = messaging;
        this.auctionProperties = auctionProperties;
        this.clock = clock;
    }

    /**
     * Places a bid:
     * <ol>
     * <li>the auction is locked and synced with the clock;</li>
     * <li>the State decides if bids are allowed (only active auctions);</li>
     * <li>currency must be the configured one and the amount must beat
     * max(highest bid, base price) by at least the minimum increment (HTTP 422);</li>
     * <li>the AI Facade reviews it: flooding is rejected (HTTP 429), huge amounts are accepted
     * but reported to {@code /topic/admin/anomalies};</li>
     * <li>after saving, a BID_PLACED event is published to the observers.</li>
     * </ol>
     */
    @Transactional
    public BidResponse placeBid(Integer auctionId, BidRequest request) {
        Auction auction = auctionService.getSyncedAuctionForUpdate(auctionId);
        auctionService.contextFor(auction).placeBid(request);
        Visitor visitor = visitorService.getVisitor(request.visitorId());

        String currency = auctionProperties.currency().toUpperCase(Locale.ROOT);
        if (!currency.equals(request.currency().trim().toUpperCase(Locale.ROOT))) {
            throw BusinessRuleException.unprocessable("Currency must be " + currency);
        }

        Bid currentTop = bidRepository.findFirstByAuctionAuctionIdOrderByAmountDescBidDateAsc(auctionId)
                .orElse(null);
        BigDecimal reference = currentTop == null ? auction.getBasePrice()
                : currentTop.getAmount().max(auction.getBasePrice());
        BigDecimal minimum = reference.add(auctionProperties.minIncrement());
        if (request.amount().compareTo(minimum) < 0) {
            throw BusinessRuleException.unprocessable("The bid must be at least " + minimum + " " + currency);
        }

        BidAssessment assessment = aiFacade.assessBid(auction, request.amount(), visitor.getVisitorId());
        if (assessment.verdict() == BidVerdict.REJECTED) {
            throw BusinessRuleException.tooManyRequests(assessment.reason());
        }

        Bid bid = new Bid();
        bid.setAuction(auction);
        bid.setVisitor(visitor);
        bid.setAmount(request.amount());
        bid.setCurrency(currency);
        bid.setBidDate(LocalDateTime.now(clock));
        BidResponse saved = AuctionMapper.toResponse(bidRepository.save(bid));

        if (assessment.verdict() == BidVerdict.SUSPICIOUS) {
            log.warn("Suspicious bid {} on auction {} by visitor {}: {}", saved.bidId(), auctionId,
                    visitor.getVisitorId(), assessment.reason());
            messaging.convertAndSend(ANOMALIES_TOPIC, new AnomalyAlert(auctionId, saved.bidId(),
                    visitor.getVisitorId(), saved.amount(), assessment.reason(), saved.bidDate()));
        }

        Integer previousTopBidderId = currentTop == null ? null : currentTop.getVisitor().getVisitorId();
        eventPublisher.publish(AuctionEvent.bidPlaced(auctionId, auction.getArtwork().getArtworkId(), saved,
                previousTopBidderId));
        return saved;
    }

    /** Bids of an auction, highest first. */
    @Transactional
    public List<BidResponse> findByAuction(Integer auctionId) {
        auctionService.getSyncedAuction(auctionId);
        return bidRepository.findByAuctionWithVisitor(auctionId).stream()
                .map(AuctionMapper::toResponse)
                .toList();
    }
}
