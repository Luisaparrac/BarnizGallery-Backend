package com.barnizgallery.backend.patterns.adapter;

import com.barnizgallery.backend.dto.AiPriceSuggestion;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.exception.AiServiceException;

/**
 * <b>Adapter pattern – default implementation of the Target.</b>
 * <p>
 * Used when {@code AI_SERVICE_URL} or {@code AI_SERVICE_KEY} is empty. It never calls the network: callers check
 * {@link #isAvailable()} and use the fixed rules; a direct call fails with {@code 503 feature_disabled}.
 */
public class DisabledAiServiceGateway implements AiServiceGateway {

    @Override
    public boolean isAvailable() {
        return false;
    }

    @Override
    public AiPriceSuggestion suggestPrice(AiPricingRequest request) {
        throw disabled();
    }

    @Override
    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        throw disabled();
    }

    private static AiServiceException disabled() {
        return new AiServiceException(503, "feature_disabled", "The AI service is not configured");
    }
}
