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
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.Auction;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.mapper.AiRequestMapper;
import com.barnizgallery.backend.mapper.ArtworkMapper;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;
import com.barnizgallery.backend.patterns.strategy.RecommendationResult;
import com.barnizgallery.backend.patterns.strategy.RecommendationStrategyResolver;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.BidRepository;
import com.barnizgallery.backend.service.AppSettingsService;

/**
 * <b>Facade pattern – Facade.</b>
 * <p>
 * Single entry point for everything related to AI: store recommendations, base price
 * suggestions and bid review. Behind it there are several subsystems (the recommendation
 * strategies, the AI text adapter, the separate AI service, the repositories and the auction
 * settings); the rest of the system only calls these simple methods and does not know whether
 * AI is enabled or which provider is used.
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

    static final int MAX_PRICE_COMPARABLES = 500;

    private final RecommendationStrategyResolver strategyResolver;
    private final AiTextClient aiClient;
    private final AiServiceGateway aiService;
    private final BidRepository bidRepository;
    private final ArtworkRepository artworkRepository;
    private final AppSettingsService settings;

    public AiFacade(RecommendationStrategyResolver strategyResolver, AiTextClient aiClient, AiServiceGateway aiService,
            BidRepository bidRepository, ArtworkRepository artworkRepository, AppSettingsService settings) {
        this.strategyResolver = strategyResolver;
        this.aiClient = aiClient;
        this.aiService = aiService;
        this.bidRepository = bidRepository;
        this.artworkRepository = artworkRepository;
        this.settings = settings;
    }

    /** True when the AI text client or the separate AI service is available. */
    public boolean isAiEnabled() {
        return aiClient.isEnabled() || aiService.isAvailable();
    }

    /** Recommends stores with the default strategy (hybrid). */
    public List<RecommendationResult> recommendStores(User user) {
        return recommendStores(user, null);
    }

    /** Recommends stores with the strategy chosen by name (Strategy pattern). */
    public List<RecommendationResult> recommendStores(User user, String strategyName) {
        return strategyResolver.resolve(strategyName).recommend(user);
    }

    /** Recommends available artworks with the strategy chosen by name (Strategy pattern). */
    public List<RecommendationResult> recommendArtworks(User user, String strategyName) {
        return strategyResolver.resolve(strategyName).recommendArtworks(user);
    }

    /**
     * Base price suggested by the AI, in the auction currency. The AI service is tried first, then the AI text
     * client. Returns empty when neither gives a valid price (AI disabled, fewer than 3 comparables, errors or
     * timeouts): a price is never invented.
     */
    public Optional<BigDecimal> suggestBasePrice(Artwork artwork) {
        Optional<BigDecimal> fromService = suggestWithAiService(artwork);
        if (fromService.isPresent() || !aiClient.isEnabled()) {
            return fromService;
        }
        String prompt = "Currency: " + settings.text(AppSettingsService.CURRENCY_DEFAULT)
                + "\nTitle: " + artwork.getTitleEn() + " / " + artwork.getTitleEs()
                + "\nTechnique: " + artwork.getTechnique()
                + "\nDimensions (cm): " + artwork.getWidthCm() + " x " + artwork.getHeightCm() + " x "
                + artwork.getDepthCm() + (artwork.getDimensionsLegacy() == null ? ""
                        : " (" + artwork.getDimensionsLegacy() + ")")
                + "\nColors: " + ArtworkMapper.colorCodes(artwork)
                + "\nMotifs: " + ArtworkMapper.motifCodes(artwork);
        try {
            return parsePrice(aiClient.complete(PRICE_SYSTEM_PROMPT, prompt));
        } catch (RuntimeException ex) {
            log.warn("AI price suggestion failed for artwork {}: {}", artwork.getArtworkId(), ex.getMessage());
            return Optional.empty();
        }
    }

    private Optional<BigDecimal> suggestWithAiService(Artwork artwork) {
        if (!aiService.isAvailable() || artwork.getShape() == null) {
            return Optional.empty();
        }
        String currency = settings.text(AppSettingsService.CURRENCY_DEFAULT);
        List<Artwork> comparables = artworkRepository.findPriceComparables(currency, artwork.getArtworkId(),
                PageRequest.of(0, MAX_PRICE_COMPARABLES));
        try {
            BigDecimal price = aiService.suggestPrice(
                    AiRequestMapper.toPricingRequest(artwork, comparables, currency)).suggestedPrice();
            return price != null && price.signum() > 0 ? Optional.of(price.setScale(2, RoundingMode.HALF_UP))
                    : Optional.empty();
        } catch (AiServiceException ex) {
            log.info("AI service gave no price for artwork {}: {}", artwork.getArtworkId(), ex.getCode());
            return Optional.empty();
        }
    }

    /**
     * Reviews a bid before it is saved. The deterministic rules always run:
     * <ul>
     * <li>more than {@code auction.max_bids_per_minute} bids of the user in 60 s → REJECTED</li>
     * <li>amount above {@code auction.suspicious_multiplier} × (highest bid or base price) → SUSPICIOUS</li>
     * </ul>
     * When AI is enabled it can also mark the bid as SUSPICIOUS (never reject it).
     */
    public BidAssessment assessBid(Auction auction, BigDecimal amount, Long userId) {

        long recentBids = bidRepository.countByUserUserIdAndBidDateAfter(userId,
                LocalDateTime.now().minusSeconds(60));
        int maxBidsPerMinute = settings.integer(AppSettingsService.AUCTION_MAX_BIDS_PER_MINUTE);
        if (recentBids >= maxBidsPerMinute) {
            return BidAssessment.rejected("Too many bids: the limit is " + maxBidsPerMinute
                    + " bids per minute");
        }

        BigDecimal suspiciousMultiplier = settings.decimal(AppSettingsService.AUCTION_SUSPICIOUS_MULTIPLIER);
        BigDecimal highest = bidRepository.findHighestAmount(auction.getAuctionId());
        BigDecimal reference = highest != null ? highest : auction.getBasePrice();
        if (reference != null && reference.signum() > 0
                && amount.compareTo(reference.multiply(suspiciousMultiplier)) > 0) {
            return BidAssessment.suspicious("Amount " + amount + " is more than " + suspiciousMultiplier
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
