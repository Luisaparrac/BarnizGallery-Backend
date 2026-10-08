package com.barnizgallery.backend.patterns.strategy;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Component;

import com.barnizgallery.backend.model.entity.Visitor;
import com.barnizgallery.backend.patterns.factorymethod.InteractionFactoryProvider;
import com.barnizgallery.backend.repository.InteractionRepository;
import com.barnizgallery.backend.repository.InteractionRepository.RoomInteraction;

/**
 * <b>Strategy pattern – ConcreteStrategy "interactions".</b>
 * <p>
 * Adds, per room, the points of the visitor's interactions:
 * weight of the action (from the Factory Method creators) × duration factor.
 * The duration factor goes from 1 (no duration) to 2 (5 minutes or more).
 * The room with most points gets 100 and the others are proportional.
 */
@Component
public class InteractionBasedStrategy implements RecommendationStrategy {

    /** Durations above this value do not add more interest. */
    static final int MAX_COUNTED_SECONDS = 300;

    private final InteractionRepository interactionRepository;
    private final InteractionFactoryProvider factoryProvider;

    public InteractionBasedStrategy(InteractionRepository interactionRepository,
            InteractionFactoryProvider factoryProvider) {
        this.interactionRepository = interactionRepository;
        this.factoryProvider = factoryProvider;
    }

    @Override
    public String name() {
        return "interactions";
    }

    @Override
    public List<RecommendationResult> recommend(Visitor visitor) {
        Map<Integer, Double> pointsByRoom = new LinkedHashMap<>();
        Map<Integer, Integer> countByRoom = new LinkedHashMap<>();
        for (RoomInteraction interaction : interactionRepository.findRoomInteractions(visitor.getVisitorId())) {
            double points = factoryProvider.weightOf(interaction.getAction())
                    * durationFactor(interaction.getDurationSeconds());
            pointsByRoom.merge(interaction.getRoomId(), points, Double::sum);
            countByRoom.merge(interaction.getRoomId(), 1, Integer::sum);
        }
        double max = pointsByRoom.values().stream().mapToDouble(Double::doubleValue).max().orElse(0);
        if (max <= 0) {
            return List.of();
        }

        List<RecommendationResult> results = new ArrayList<>();
        pointsByRoom.forEach((roomId, points) -> {
            int count = countByRoom.get(roomId);
            String reason = LocalizedText.pick(visitor,
                    "Interactuaste " + count + " veces con obras de esta sala",
                    "You interacted " + count + " times with artworks in this room");
            results.add(RecommendationResult.of(roomId, 100.0 * points / max, reason));
        });
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
