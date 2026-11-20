package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.Artwork;
import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.repository.ArtworkRepository;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.StoreInteraction;

/**
 * <b>Strategy pattern – ConcreteStrategy "interactions".</b>
 * <p>
 * Adds, per store, the points of the user's interactions with the store itself or with its artworks:
 * weight of the action (from the Factory Method creators) × duration factor.
 * The duration factor goes from 1 (no duration) to 2 (5 minutes or more).
 * The store with most points gets 100 and the others are proportional.
 */
@Component
public class InteractionBasedStrategy implements RecommendationStrategy {

    /** Durations above this value do not add more interest. */
    static final int MAX_COUNTED_SECONDS = 300;

    private final InteractionRepository interactionRepository;
    private final InteractionFactoryProvider factoryProvider;
    private final ArtworkRepository artworkRepository;

    public InteractionBasedStrategy(InteractionRepository interactionRepository,
            InteractionFactoryProvider factoryProvider, ArtworkRepository artworkRepository) {
        this.interactionRepository = interactionRepository;
        this.factoryProvider = factoryProvider;
        this.artworkRepository = artworkRepository;
    }

    @Override
    public String name() {
        return "interactions";
    }

    @Override
    public List<RecommendationResult> recommend(User user) {
        Map<Long, Double> pointsByStore = new LinkedHashMap<>();
        Map<Long, Integer> countByStore = new LinkedHashMap<>();
        for (StoreInteraction interaction : interactionRepository.findStoreInteractions(user.getUserId())) {
            double points = factoryProvider.weightOf(interaction.getAction())
                    * durationFactor(interaction.getDurationSeconds());
            pointsByStore.merge(interaction.getStoreId(), points, Double::sum);
            countByStore.merge(interaction.getStoreId(), 1, Integer::sum);
        }
        double max = pointsByStore.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        if (max <= 0) {
            return List.of();
        }

        List<RecommendationResult> results = new ArrayList<>();
        pointsByStore.forEach((storeId, points) -> {
            int count = countByStore.get(storeId);
            String reason = LocalizedText.pick(user,
                    "Interactuaste " + count + " veces con esta tienda o sus obras",
                    "You interacted " + count + " times with this store or its artworks");
            results.add(RecommendationResult.of(storeId, 100.0 * points / max, reason));
        });
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    /** Available artworks of the stores the user interacted with; each one gets the score of its store. */
    @Override
    public List<RecommendationResult> recommendArtworks(User user) {
        Map<Long, RecommendationResult> byStore = new LinkedHashMap<>();
        recommend(user).forEach(result -> byStore.put(result.storeId(), result));
        if (byStore.isEmpty()) {
            return List.of();
        }
        List<RecommendationResult> results = new ArrayList<>();
        for (Artwork artwork : artworkRepository.findByDeletedAtIsNullAndStatusInOrderByArtworkIdAsc(
                ProfileBasedStrategy.AVAILABLE, AiServiceStrategy.CANDIDATES)) {
            RecommendationResult store = byStore.get(artwork.getStore().getStoreId());
            if (store != null) {
                results.add(RecommendationResult.ofArtwork(artwork.getArtworkId(), store.score().doubleValue(),
                        store.reason()));
            }
        }
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    static double durationFactor(Integer durationSeconds) {
        if (durationSeconds == null || durationSeconds <= 0) {
            return 1.0;
        }
        return 1.0 + Math.min(durationSeconds, MAX_COUNTED_SECONDS) / (double) MAX_COUNTED_SECONDS;
    }
}
