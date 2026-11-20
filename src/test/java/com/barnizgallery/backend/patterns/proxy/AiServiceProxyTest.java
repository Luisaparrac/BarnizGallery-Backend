package com.barnizgallery.backend.patterns.proxy;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.dto.AiRecommendationRequest;
import com.barnizgallery.backend.dto.AiRecommendationResponse;
import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;

class AiServiceProxyTest {

    private final AiServiceGateway real = mock(AiServiceGateway.class);
    private static final Instant NOW = Instant.parse("2026-11-20T12:00:00Z");
    private final Clock clock = mock(Clock.class);
    private final AiServiceProxy proxy = new AiServiceProxy(real, clock, Duration.ofMinutes(2), Duration.ofSeconds(30));
    private final AiRecommendationRequest request = new AiRecommendationRequest(null, List.of(), List.of(), 5);

    @Test
    void theSameRequestIsAnsweredFromTheCacheUntilItExpires() {
        when(clock.instant()).thenReturn(NOW);
        AiRecommendationResponse answer = new AiRecommendationResponse("content", List.of());
        when(real.recommend(request)).thenReturn(answer);

        assertThat(proxy.recommend(request)).isSameAs(answer);
        assertThat(proxy.recommend(request)).isSameAs(answer);
        verify(real, times(1)).recommend(request);

        when(clock.instant()).thenReturn(NOW.plus(Duration.ofMinutes(3)));
        proxy.recommend(request);
        verify(real, times(2)).recommend(request);
    }

    @Test
    void aServerFailureStopsTheCallsForTheCoolDownButBusinessErrorsDoNot() {
        when(clock.instant()).thenReturn(NOW);
        when(real.recommend(request)).thenThrow(new AiServiceException(504, "provider_timeout", "slow"));

        assertThatThrownBy(() -> proxy.recommend(request)).hasMessage("slow");
        assertThatThrownBy(() -> proxy.recommend(request))
                .isInstanceOfSatisfying(AiServiceException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("service_cooling_down"));
        verify(real, times(1)).recommend(request);

        when(clock.instant()).thenReturn(NOW.plusSeconds(31));
        doThrow(new AiServiceException(422, "not_enough_data", "few")).when(real).recommend(request);
        assertThatThrownBy(() -> proxy.recommend(request)).hasMessage("few");
        assertThatThrownBy(() -> proxy.recommend(request)).hasMessage("few");
        verify(real, times(3)).recommend(request);
    }
}
