package com.barnizgallery.backend.patterns.facade;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.config.AppProperties;
import com.barnizgallery.backend.model.entity.Artwork;
import com.barnizgallery.backend.model.entity.Auction;
import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.patterns.strategy.RecommendationStrategyResolver;
import com.barnizgallery.backend.repository.BidRepository;

/**
 * <b>Facade pattern – Facade.</b>
 * <p>
 * Single entry point for everything related to AI: room recommendations, base price
 * suggestions and bid review. Behind it there are several subsystems (the recommendation
 * strategies, the AI adapter, the bid repository and the auction settings); the rest of
 * the system only calls these four simple methods and does not know whether AI is enabled
 * or which provider is used.
 */
@Component
public class AiFacade {

    private static final Logger log = LoggerFactory.getLogger(AiFacade.class);
    private static final Pattern NUMBER = Pattern.compile("\\d+(?:\\.\\d+)?");

    static final String PRICE_SYSTEM_PROMPT = """
            You are an appraiser of Barniz de Pasto Mopa-Mopa handicrafts.
            Answer ONLY with the suggested starting auction price as a plain number, without symbols.""";

    static final String BID_SYSTEM_PROMPT = """
            You review bids of an online art auction to detect anomalies.
            Answer with OK, or with SUSPICIOUS: <short reason>.""";

    private final RecommendationStrategyResolver strategyResolver;
    private final AiTextClient aiClient;
    private final BidRepository bidRepository;
    private final AppProperties properties;

    public AiFacade(RecommendationStrategyResolver strategyResolver, AiTextClient aiClient,
            BidRepository bidRepository, AppProperties properties) {
        this.strategyResolver = strategyResolver;
        this.aiClient = aiClient;
        this.bidRepository = bidRepository;
        this.properties = properties;
    }

    public boolean isAiEnabled() {
        return aiClient.isEnabled();
    }

    /** Recommends rooms with the default strategy (hybrid). */
    public List<RecommendationResult> recommendRooms(Visitor visitor) {
        return recommendRooms(visitor, null);
    }

    /** Recommends rooms with the strategy chosen by name (Strategy pattern). */
    public List<RecommendationResult> recommendRooms(Visitor visitor, String strategyName) {
        return strategyResolver.resolve(strategyName).recommend(visitor);
    }

    /**
     * Base price suggested by the AI. Returns empty when AI is disabled or the answer
     * is not a valid price: a price is never invented.
     */
    public Optional<BigDecimal> suggestBasePrice(Artwork artwork) {
        if (!aiClient.isEnabled()) {
            return Optional.empty();
        }
        String prompt = "Currency: " + properties.auction().currency()
                + "\nTitle: " + artwork.getTitleEn() + " / " + artwork.getTitleEs()
                + "\nTechnique: " + artwork.getTechnique()
                + "\nDimensions: " + artwork.getDimensions()
                + "\nColors: " + artwork.getColorTags()
                + "\nMotifs: " + artwork.getMotifTags();
        try {
            return parsePrice(aiClient.complete(PRICE_SYSTEM_PROMPT, prompt));
        } catch (RuntimeException ex) {
            log.warn("AI price suggestion failed for artwork {}: {}", artwork.getArtworkId(), ex.getMessage());
            return Optional.empty();
        }
    }

    /**
     * Reviews a bid before it is saved. The deterministic rules always run:
     * <ul>
     * <li>more than {@code app.auction.max-bids-per-minute} bids of the visitor in 60 s → REJECTED</li>
     * <li>amount above {@code app.auction.suspicious-multiplier} × (highest bid or base price) → SUSPICIOUS</li>
     * </ul>
     * When AI is enabled it can also mark the bid as SUSPICIOUS (never reject it).
     */
    public BidAssessment assessBid(Auction auction, BigDecimal amount, Integer visitorId) {
        AppProperties.Auction rules = properties.auction();

        long recentBids = bidRepository.countByVisitorVisitorIdAndBidDateAfter(visitorId,
                LocalDateTime.now().minusSeconds(60));
        if (recentBids >= rules.maxBidsPerMinute()) {
            return BidAssessment.rejected("Too many bids: the limit is " + rules.maxBidsPerMinute()
                    + " bids per minute");
        }

        BigDecimal highest = bidRepository.findHighestAmount(auction.getAuctionId());
        BigDecimal reference = highest != null ? highest : auction.getBasePrice();
        if (reference != null && reference.signum() > 0
                && amount.compareTo(reference.multiply(rules.suspiciousMultiplier())) > 0) {
            return BidAssessment.suspicious("Amount " + amount + " is more than " + rules.suspiciousMultiplier()
                    + " times the reference " + reference);
        }

        if (aiClient.isEnabled()) {
            return assessWithAi(auction, amount, reference);
        }
        return BidAssessment.ok();
    }

    private BidAssessment assessWithAi(Auction auction, BigDecimal amount, BigDecimal reference) {
        String prompt = "Auction " + auction.getAuctionId() + ", base price " + auction.getBasePrice()
                + ", current reference " + reference + ", new bid " + amount;
        try {
            String answer = aiClient.complete(BID_SYSTEM_PROMPT, prompt);
            if (answer != null && answer.trim().toUpperCase(Locale.ROOT).startsWith("SUSPICIOUS")) {
                return BidAssessment.suspicious("AI: " + answer.trim());
            }
        } catch (RuntimeException ex) {
            log.warn("AI bid review failed for auction {}: {}", auction.getAuctionId(), ex.getMessage());
        }
        return BidAssessment.ok();
    }

    static Optional<BigDecimal> parsePrice(String answer) {
        if (answer == null) {
            return Optional.empty();
        }
        Matcher matcher = NUMBER.matcher(answer.replace(",", ""));
        if (!matcher.find()) {
            return Optional.empty();
        }
        BigDecimal price = new BigDecimal(matcher.group()).setScale(2, RoundingMode.HALF_UP);
        return price.signum() > 0 ? Optional.of(price) : Optional.empty();
    }
}
