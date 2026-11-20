package com.barnizgallery.backend.patterns.builder;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Optional;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.AuctionStatus;
import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.BusinessRuleException;
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
 *         .currency("USD")
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
    private String currency;
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

    /** ISO 4217 code of the base price and of every bid. */
    public AuctionBuilder currency(String currency) {
        this.currency = currency;
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
            throw new BusinessRuleException(ErrorCode.ARTWORK_REQUIRED);
        }
        if (artwork.getStatus() != ArtworkStatus.EXHIBITED) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_NOT_EXHIBITED, artwork.getArtworkId());
        }
        if (auctionRepository.existsByArtworkArtworkIdAndStatusIn(artwork.getArtworkId(),
                EnumSet.of(AuctionStatus.SCHEDULED, AuctionStatus.ACTIVE))) {
            throw new BusinessRuleException(ErrorCode.ARTWORK_ALREADY_IN_AUCTION, artwork.getArtworkId());
        }
        if (currency == null || !currency.matches("[A-Za-z]{3}")) {
            throw BusinessRuleException.unprocessable(ErrorCode.CURRENCY_REQUIRED);
        }
        if (startDate == null || endDate == null) {
            throw BusinessRuleException.unprocessable(ErrorCode.AUCTION_DATES_REQUIRED);
        }
        if (!endDate.isAfter(startDate)) {
            throw BusinessRuleException.unprocessable(ErrorCode.AUCTION_END_BEFORE_START);
        }
        LocalDateTime now = LocalDateTime.now(clock);
        if (!endDate.isAfter(now)) {
            throw BusinessRuleException.unprocessable(ErrorCode.AUCTION_END_IN_PAST);
        }

        BigDecimal price = resolveBasePrice();
        if (price.signum() < 0) {
            throw BusinessRuleException.unprocessable(ErrorCode.BASE_PRICE_NEGATIVE);
        }

        Auction auction = new Auction();
        auction.setArtwork(artwork);
        auction.setBasePrice(price);
        auction.setCurrency(currency.toUpperCase(Locale.ROOT));
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
            throw BusinessRuleException.unprocessable(ErrorCode.BASE_PRICE_REQUIRED);
        }
        Optional<BigDecimal> suggested = priceAdvisor.suggestBasePrice(artwork);
        return suggested.orElseThrow(() -> BusinessRuleException.unprocessable(
                ErrorCode.BASE_PRICE_NOT_SUGGESTED));
    }
}
