package com.barnizgallery.backend.patterns.strategy;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import com.barnizgallery.backend.exception.BusinessRuleException;
import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.patterns.adapter.AiTextClient;

/**
 * Chooses the {@link RecommendationStrategy} by name at runtime.
 * Spring injects every strategy, so adding a new one needs no change here.
 */
@Component
public class RecommendationStrategyResolver {

    public static final String DEFAULT_STRATEGY = "hybrid";

    private final Map<String, RecommendationStrategy> strategies = new LinkedHashMap<>();
    private final AiTextClient aiClient;

    public RecommendationStrategyResolver(List<RecommendationStrategy> strategies, AiTextClient aiClient) {
        strategies.forEach(strategy -> this.strategies.put(strategy.name(), strategy));
        this.aiClient = aiClient;
    }

    /**
     * @param name profile, interactions, hybrid or ai; null or blank means {@value #DEFAULT_STRATEGY}
     */
    public RecommendationStrategy resolve(String name) {
        String key = (name == null || name.isBlank()) ? DEFAULT_STRATEGY : name.trim().toLowerCase(Locale.ROOT);
        RecommendationStrategy strategy = strategies.get(key);
        if (strategy == null) {
            throw new BusinessRuleException("Unknown strategy '" + name + "'. Use one of " + strategies.keySet(),
                    HttpStatus.BAD_REQUEST);
        }
        if ("ai".equals(key) && !aiClient.isEnabled()) {
            throw new FeatureDisabledException("The 'ai' recommendation strategy needs an AI provider");
        }
        return strategy;
    }
}
