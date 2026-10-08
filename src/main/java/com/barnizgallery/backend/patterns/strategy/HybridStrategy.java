package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.repository.InteractionRepository;

/**
 * <b>Strategy pattern – ConcreteStrategy "hybrid" (default).</b>
 * <p>
 * Combines the other two strategies: when the visitor has enough interactions
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
    public List<RecommendationResult> recommend(Visitor visitor) {
        List<RecommendationResult> byProfile = profileStrategy.recommend(visitor);
        if (interactionRepository.countByVisitorVisitorId(visitor.getVisitorId()) < MIN_INTERACTIONS) {
            return byProfile;
        }
        List<RecommendationResult> byInteractions = interactionStrategy.recommend(visitor);

        Map<Integer, RecommendationResult> profileMap = toMap(byProfile);
        Map<Integer, RecommendationResult> interactionMap = toMap(byInteractions);
        Set<Integer> roomIds = new LinkedHashSet<>(interactionMap.keySet());
        roomIds.addAll(profileMap.keySet());

        List<RecommendationResult> results = new ArrayList<>();
        for (Integer roomId : roomIds) {
            RecommendationResult fromInteractions = interactionMap.get(roomId);
            RecommendationResult fromProfile = profileMap.get(roomId);
            double score = INTERACTION_WEIGHT * scoreOf(fromInteractions) + PROFILE_WEIGHT * scoreOf(fromProfile);
            List<String> reasons = new ArrayList<>();
            if (fromInteractions != null) {
                reasons.add(fromInteractions.reason());
            }
            if (fromProfile != null) {
                reasons.add(fromProfile.reason());
            }
            results.add(RecommendationResult.of(roomId, score, String.join(" · ", reasons)));
        }
        results.sort(Comparator.comparing(RecommendationResult::score).reversed());
        return results;
    }

    private static Map<Integer, RecommendationResult> toMap(List<RecommendationResult> results) {
        Map<Integer, RecommendationResult> map = new LinkedHashMap<>();
        results.forEach(r -> map.put(r.roomId(), r));
        return map;
    }

    private static double scoreOf(RecommendationResult result) {
        return result == null ? 0 : result.score().doubleValue();
    }
}
