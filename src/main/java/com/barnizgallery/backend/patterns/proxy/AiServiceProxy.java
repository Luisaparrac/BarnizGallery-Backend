package com.barnizgallery.backend.patterns.proxy;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.barnizgallery.backend.dto.AiPriceSuggestion;
import com.barnizgallery.backend.dto.AiPricingRequest;
import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.dto.AiSimilarRequest;
import com.barnizgallery.backend.dto.AiSimilarResponse;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;

/**
 * <b>Proxy pattern – Proxy.</b>
 * <p>
 * Stands in front of the real {@link AiServiceGateway} (the HTTP client) with the same interface, so callers do
 * not change. It adds two controls the real subject does not have:
 * <ul>
 * <li><b>cache</b>: the same request within {@code ttl} is answered from memory (the AI service sleeps on Render
 * and every call costs time);</li>
 * <li><b>protection</b>: after a failure that means "the service is down" (timeouts, 5xx) it does not call again
 * for {@code coolDown}, and fails fast with {@code service_cooling_down}, so the callers fall back at once.</li>
 * </ul>
 * Business errors (for example {@code not_enough_data}) pass through and do not open the cool-down.
 */
public class AiServiceProxy implements AiServiceGateway {

    private static final Logger log = LoggerFactory.getLogger(AiServiceProxy.class);

    private final AiServiceGateway realGateway;
    private final Clock clock;
    private final Duration ttl;
    private final Duration coolDown;
    private final Map<Object, CachedAnswer> cache = new ConcurrentHashMap<>();
    private volatile Instant closedUntil = Instant.MIN;

    public AiServiceProxy(AiServiceGateway realGateway, Clock clock, Duration ttl, Duration coolDown) {
        this.realGateway = realGateway;
        this.clock = clock;
        this.ttl = ttl;
        this.coolDown = coolDown;
    }

    @Override
    public boolean isAvailable() {
        return realGateway.isAvailable();
    }

    @Override
    public AiPriceSuggestion suggestPrice(AiPricingRequest request) {
        return (AiPriceSuggestion) call(request, () -> realGateway.suggestPrice(request));
    }

    @Override
    public AiRecommendationResponse recommend(AiRecommendationRequest request) {
        return (AiRecommendationResponse) call(request, () -> realGateway.recommend(request));
    }

    @Override
    public AiSimilarResponse similar(AiSimilarRequest request) {
        return (AiSimilarResponse) call(request, () -> realGateway.similar(request));
    }

    private Object call(Object request, Supplier<Object> realCall) {
        Instant now = clock.instant();
        CachedAnswer cached = cache.get(request);
        if (cached != null && cached.validUntil().isAfter(now)) {
            return cached.answer();
        }
        if (closedUntil.isAfter(now)) {
            throw new AiServiceException(503, "service_cooling_down", "The AI service failed recently");
        }
        try {
            Object answer = realCall.get();
            cache.entrySet().removeIf(entry -> !entry.getValue().validUntil().isAfter(now));
            cache.put(request, new CachedAnswer(answer, now.plus(ttl)));
            return answer;
        } catch (AiServiceException ex) {
            if (ex.getStatus() >= 500) {
                closedUntil = now.plus(coolDown);
                log.warn("AI service failed ({}); not calling it for {} s", ex.getCode(), coolDown.toSeconds());
            }
            throw ex;
        }
    }
}
