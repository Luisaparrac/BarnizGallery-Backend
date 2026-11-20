package com.barnizgallery.backend.patterns.adapter;

import com.barnizgallery.backend.dto.AiPriceSuggestion;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.dto.AiSimilarRequest;
import com.barnizgallery.backend.dto.AiSimilarResponse;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the backend uses to talk to the separate MUSEO AI service (prices and recommendations).
 * It is independent of {@link AiTextClient}: the service exposes concrete functions, not free text.
 * Every call can fail with {@link com.barnizgallery.backend.exception.AiServiceException}; callers must fall back
 * to the fixed rules.
 */
public interface AiServiceGateway {

    /** False when the service is not configured: callers use the fixed rules without calling it. */
    boolean isAvailable();

    /** {@code POST /v1/pricing/suggest}. Fails with {@code not_enough_data} with fewer than 3 comparables. */
    AiPriceSuggestion suggestPrice(AiPricingRequest request);

    /** {@code POST /v1/recommendations}. */
    AiRecommendationResponse recommend(AiRecommendationRequest request);

    /**
     * {@code POST /v1/similar}: artworks that look like one artwork, best first. Fails with {@code not_enough_data}
     * when the artwork is not in the service's index.
     */
    AiSimilarResponse similar(AiSimilarRequest request);
}
