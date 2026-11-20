package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.dto.AiCandidate;
import com.barnizgallery.backend.dto.AiInteraction;
import com.barnizgallery.backend.dto.AiRecommendationItem;
import com.barnizgallery.backend.dto.AiRecommendationReasons;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiTasteProfile;
import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.mapper.AiRequestMapper;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.repository.ArtworkInteractionCount;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.TasteProfileRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "ai-service".</b>
 * <p>
 * Sends the available artworks (with their catalog codes), the taste profile and the interactions of the user to
 * the AI service, which scores artworks from 0 to 1. The scores of each store are added up (as 0-100) and
 * rescaled so the best store stays at 100 at most. When the service is not configured, fails or there are no
 * available artworks, it delegates to {@link HybridStrategy}: a failure of the AI never breaks the flow.
 */
@Component
public class AiServiceStrategy implements RecommendationStrategy {

    static final int MAX_RESULTS = 100;
    static final int MAX_CANDIDATES = 2000;
    /** At most {@value #MAX_CANDIDATES} available artworks are compared. */
    static final Pageable CANDIDATES = PageRequest.of(0, MAX_CANDIDATES);
    static final int MAX_INTERACTIONS = 1000;

    private static final Logger log = LoggerFactory.getLogger(AiServiceStrategy.class);

    private final AiServiceGateway aiService;
    private final HybridStrategy fallback;
    private final ArtworkRepository artworkRepository;
    private final TasteProfileRepository tasteProfileRepository;
    private final InteractionRepository interactionRepository;

    public AiServiceStrategy(AiServiceGateway aiService, HybridStrategy fallback, ArtworkRepository artworkRepository,
            TasteProfileRepository tasteProfileRepository, InteractionRepository interactionRepository) {
        this.aiService = aiService;
        this.fallback = fallback;
        this.artworkRepository = artworkRepository;
        this.tasteProfileRepository = tasteProfileRepository;
        this.interactionRepository = interactionRepository;
    }

    @Override
    public String name() {
        return "ai-service";
    }

    @Override
    public List<RecommendationResult> recommend(User user) {
        if (!aiService.isAvailable()) {
            return fallback.recommend(user);
        }
        List<Artwork> available = artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(ArtworkStatus.PUBLIC,
                CANDIDATES);
        if (available.isEmpty()) {
            return fallback.recommend(user);
        }
        try {
            List<AiRecommendationItem> items = aiService.recommend(request(user, available)).items();
            return byStore(user, items == null ? List.of() : items, available);
        } catch (AiServiceException ex) {
            log.warn("Recommendations for user {} without the AI service: {}", user.getUserId(), ex.getCode());
            return fallback.recommend(user);
        }
    }

    /** Artworks ranked by the AI service (score 0-1 → 0-100); falls back to the hybrid strategy. */
    @Override
    public List<RecommendationResult> recommendArtworks(User user) {
        if (!aiService.isAvailable()) {
            return fallback.recommendArtworks(user);
        }
        List<Artwork> available = artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(ArtworkStatus.PUBLIC,
                CANDIDATES);
        if (available.isEmpty()) {
            return fallback.recommendArtworks(user);
        }
        try {
            List<AiRecommendationItem> items = aiService.recommend(request(user, available)).items();
            return (items == null ? List.<AiRecommendationItem>of() : items).stream()
                    .filter(item -> item.score() > 0)
                    .map(item -> RecommendationResult.ofArtwork(item.artworkId(), item.score() * 100,
                            AiReasonText.describe(user, item.reasons() == null ? List.of() : List.of(item.reasons()))))
                    .toList();
        } catch (AiServiceException ex) {
            log.warn("Artwork recommendations for user {} without the AI service: {}", user.getUserId(), ex.getCode());
            return fallback.recommendArtworks(user);
        }
    }

    private AiRecommendationRequest request(User user, List<Artwork> available) {
        Map<Long, Long> popularity = interactionRepository.countGroupedByArtwork().stream()
                .collect(Collectors.toMap(ArtworkInteractionCount::getArtworkId,
                        ArtworkInteractionCount::getInteractionCount));
        Set<Long> candidateIds = available.stream().map(Artwork::getArtworkId).collect(Collectors.toSet());
        AiTasteProfile profile = tasteProfileRepository.findByUserUserId(user.getUserId())
                .map(AiRequestMapper::toProfile)
                .orElse(null);
        // The service only uses interactions with artworks it receives as candidates.
        List<AiInteraction> interactions = interactionRepository.findHistory(user.getUserId()).stream()
                .filter(i -> i.getArtwork() != null && candidateIds.contains(i.getArtwork().getArtworkId()))
                .map(AiRequestMapper::toInteraction)
                .flatMap(Optional::stream)
                .limit(MAX_INTERACTIONS)
                .toList();
        List<AiCandidate> candidates = available.stream()
                .map(a -> AiRequestMapper.toCandidate(a, popularity.getOrDefault(a.getArtworkId(), 0L)))
                .toList();
        return new AiRecommendationRequest(profile, interactions, candidates, MAX_RESULTS);
    }

    /** Adds up the scores of the artworks of each store (0-1 → 0-100) and writes the reason. */
    private static List<RecommendationResult> byStore(User user, List<AiRecommendationItem> items,
            List<Artwork> available) {
        Map<Long, Long> storeOfArtwork = available.stream()
                .collect(Collectors.toMap(Artwork::getArtworkId, a -> a.getStore().getStoreId()));
        Map<Long, Double> points = new HashMap<>();
        Map<Long, List<AiRecommendationReasons>> reasons = new HashMap<>();
        for (AiRecommendationItem item : items) {
            Long storeId = storeOfArtwork.get(item.artworkId());
            if (storeId == null || item.score() <= 0) {
                continue;
            }
            points.merge(storeId, item.score() * 100, Double::sum);
            if (item.reasons() != null) {
                reasons.computeIfAbsent(storeId, id -> new ArrayList<>()).add(item.reasons());
            }
        }
        double top = points.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        double scale = top > 100 ? 100 / top : 1;
        return points.entrySet().stream()
                .sorted(Map.Entry.<Long, Double>comparingByValue().reversed()
                        .thenComparing(Map.Entry.comparingByKey(Comparator.naturalOrder())))
                .map(e -> RecommendationResult.of(e.getKey(), e.getValue() * scale,
                        AiReasonText.describe(user, reasons.getOrDefault(e.getKey(), List.of()))))
                .toList();
    }
}
