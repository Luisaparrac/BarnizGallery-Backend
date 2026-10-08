package com.barnizgallery.backend.patterns.builder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Optional;

import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.enums.ArtworkStatus;
import com.barnizgallery.backend.model.enums.AuctionStatus;
import com.barnizgallery.backend.patterns.facade.AiFacade;
import com.barnizgallery.backend.repository.AuctionRepository;

/**
 * <b>Builder pattern – Builder.</b>
 * <p>
 * Creates an {@link Auction} step by step. An auction has optional parts (the base price
 * can come from the request or be suggested by the AI) and several rules, so a constructor
 * with many parameters would be fragile. Each method sets one part and returns the builder
 * (fluent API); {@link #build()} checks all the rules and returns the finished auction.
 * <pre>
 * Auction auction = new AuctionBuilder(auctionRepository, clock)
 *         .forArtwork(artwork)
 *         .basePrice(new BigDecimal("500"))
 *         .startsAt(start)
 *         .endsAt(end)
 *         .build();
 * </pre>
 */
public class AuctionBuilder {

    private final AuctionRepository auctionRepository;
    private final Clock clock;

    private Artwork artwork;
    private BigDecimal basePrice;
    private AiFacade priceAdvisor;
    private LocalDateTime startDate;
    private LocalDateTime endDate;

    public AuctionBuilder(AuctionRepository auctionRepository, Clock clock) {
        this.auctionRepository = auctionRepository;
        this.clock = clock;
    }

    public AuctionBuilder forArtwork(Artwork artwork) {
        this.artwork = artwork;
        return this;
    }

    public AuctionBuilder basePrice(BigDecimal basePrice) {
        this.basePrice = basePrice;
        return this;
    }

    /** If no base price is given, ask the AI for one when building. */
    public AuctionBuilder useSuggestedBasePrice(AiFacade aiFacade) {
        this.priceAdvisor = aiFacade;
        return this;
    }

    public AuctionBuilder startsAt(LocalDateTime startDate) {
        this.startDate = startDate;
        return this;
    }

    public AuctionBuilder endsAt(LocalDateTime endDate) {
        this.endDate = endDate;
        return this;
    }

    /**
     * Validates the parts and creates the auction (not saved yet).
     * Initial status: active if the start date has already arrived, otherwise scheduled.
     */
    public Auction build() {
        if (artwork == null) {
            throw new BusinessRuleException("The artwork is required");
        }
        if (artwork.getStatus() != ArtworkStatus.EXHIBITED) {
            throw new BusinessRuleException("Artwork " + artwork.getArtworkId()
                    + " must be exhibited to be auctioned (current status: " + artwork.getStatus() + ")");
        }
        if (auctionRepository.existsByArtworkArtworkIdAndStatusIn(artwork.getArtworkId(),
                EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))) {
            throw new BusinessRuleException("Artwork " + artwork.getArtworkId()
                    + " already has a scheduled or active auction");
        }
        if (startDate == null || endDate == null) {
            throw BusinessRuleException.unprocessable("startDate and endDate are required");
        }
        if (!endDate.isAfter(startDate)) {
            throw BusinessRuleException.unprocessable("endDate must be after startDate");
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!endDate.isAfter(now)) {
            throw BusinessRuleException.unprocessable("endDate must be in the future");
        }

        BigDecimal price = resolveBasePrice();
        if (price.signum() < 0) {
            throw BusinessRuleException.unprocessable("basePrice must be >= 0");
        }

        Auction auction = new Auction();
        auction.setArtwork(artwork);
        auction.setBasePrice(price);
        auction.setStartDate(startDate);
        auction.setEndDate(endDate);
        auction.setStatus(startDate.isAfter(now) ? AuctionStatus.SCHEDULED : AuctionStatus.ACTIVE);
        return auction;
    }

    private BigDecimal resolveBasePrice() {
        if (basePrice != null) {
            return basePrice;
        }
        if (priceAdvisor == null || !priceAdvisor.isAiEnabled()) {
            throw BusinessRuleException.unprocessable("basePrice is required while AI is disabled");
        }
        Optional<BigDecimal> suggested = priceAdvisor.suggestBasePrice(artwork);
        return suggested.orElseThrow(() -> BusinessRuleException.unprocessable(
                "The AI could not suggest a base price; basePrice is required"));
    }
}
