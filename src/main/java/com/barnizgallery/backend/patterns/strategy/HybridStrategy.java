package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.entity.User;
import com.barnizgallery.backend.repository.InteractionRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "hybrid" (default).</b>
 * <p>
 * Combines the other two strategies: when the user has enough interactions
 * ({@value #MIN_INTERACTIONS} or more) the score is 60 % interactions + 40 % profile;
 * otherwise it uses 100 % the profile.
 */
@Component
public class HybridStrategy implements RecommendationStrategy {

    static final int MIN_INTERACTIONS = 3;
    static final double INTERACTION_WEIGHT = 0.6;
    static final double PROFILE_WEIGHT = 0.4;

    private final ProfileBasedStrategy profileStrategy;
    private final InteractionBasedStrategy interactionStrategy;
    private final InteractionRepository interactionRepository;

    public HybridStrategy(ProfileBasedStrategy profileStrategy, InteractionBasedStrategy interactionStrategy,
            InteractionRepository interactionRepository) {
        this.profileStrategy = profileStrategy;
        this.interactionStrategy = interactionStrategy;
        this.interactionRepository = interactionRepository;
    }

    @Override
    public String name() {
        return "hybrid";
    }

    @Override
    public List<RecommendationResult> recommend(User user) {
        List<RecommendationResult> byProfile = profileStrategy.recommend(user);
        if (interactionRepository.countByUserUserId(user.getUserId()) < MIN_INTERACTIONS) {
            return byProfile;
        }
        return combine(interactionStrategy.recommend(user), byProfile, false);
    }

    @Override
    public List<RecommendationResult> recommendArtworks(User user) {
        List<RecommendationResult> byProfile = profileStrategy.recommendArtworks(user);
        if (interactionRepository.countByUserUserId(user.getUserId()) < MIN_INTERACTIONS) {
            return byProfile;
        }
        return combine(interactionStrategy.recommendArtworks(user), byProfile, true);
    }

    /** 60 % interactions + 40 % profile for every store (or artwork) found by either strategy. */
    private static List<RecommendationResult> combine(List<RecommendationResult> byInteractions,
            List<RecommendationResult> byProfile, boolean artworks) {
        Map<Long, RecommendationResult> profileMap = toMap(byProfile);
        Map<Long, RecommendationResult> interactionMap = toMap(byInteractions);
        Set<Long> ids = new LinkedHashSet<>(interactionMap.keySet());
        ids.addAll(profileMap.keySet());

        List<RecommendationResult> results = new ArrayList<>();
        for (Long id : ids) {
            RecommendationResult fromInteractions = interactionMap.get(id);
            RecommendationResult fromProfile = profileMap.get(id);
            double score = INTERACTION_WEIGHT * scoreOf(fromInteractions) + PROFILE_WEIGHT * scoreOf(fromProfile);
            List<String> reasons = new ArrayList<>();
            if (fromInteractions != null) {
                reasons.add(fromInteractions.reason());
            }
            if (fromProfile != null) {
                reasons.add(fromProfile.reason());
            }
            String reason = String.join(" · ", reasons);
            results.add(artworks ? RecommendationResult.ofArtwork(id, score, reason)
                    : RecommendationResult.of(id, score, reason));
        }
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    private static Map<Long, RecommendationResult> toMap(List<RecommendationResult> results) {
        Map<Long, RecommendationResult> map = new LinkedHashMap<>();
        results.forEach(r -> map.put(r.targetId(), r));
        return map;
    }

    private static double scoreOf(RecommendationResult result) {
        return result == null ? 0 : result.score().doubleValue();
    }
}
