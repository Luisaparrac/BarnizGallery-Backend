package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.exception.AiServiceException;
import com.barnizgallery.backend.patterns.adapter.AiServiceClient;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.adapter.DisabledAiServiceGateway;

class AiServiceConfigTest {

    private static final Duration TIMEOUT = Duration.ofSeconds(10);

    private final AiServiceConfig config = new AiServiceConfig();
    private final String key = UUID.randomUUID().toString();

    @Test
    void emptyUrlOrKeyDisablesTheService() {
        assertThat(config.aiServiceGateway(new AiServiceProperties("", key, TIMEOUT)))
                .isInstanceOf(DisabledAiServiceGateway.class);
        assertThat(config.aiServiceGateway(new AiServiceProperties("http://ai.test", " ", TIMEOUT)))
                .isInstanceOf(DisabledAiServiceGateway.class);
        assertThat(config.aiServiceGateway(new AiServiceProperties(null, null, TIMEOUT)))
                .isInstanceOf(DisabledAiServiceGateway.class);
    }

    @Test
    void disabledGatewayIsNotAvailableAndNeverCallsTheNetwork() {
        AiServiceGateway gateway = config.aiServiceGateway(new AiServiceProperties("", "", TIMEOUT));

        assertThat(gateway.isAvailable()).isFalse();
        assertThatThrownBy(() -> gateway.suggestPrice(null))
                .isInstanceOfSatisfying(AiServiceException.class,
                        ex -> assertThat(ex.getCode()).isEqualTo("feature_disabled"));
        assertThatThrownBy(() -> gateway.recommend(null)).isInstanceOf(AiServiceException.class);
    }

    @Test
    void urlAndKeyEnableTheHttpClient() {
        AiServiceGateway gateway = config.aiServiceGateway(new AiServiceProperties("http://ai.test", key, TIMEOUT));

        assertThat(gateway).isInstanceOf(AiServiceClient.class);
        assertThat(gateway.isAvailable()).isTrue();
    }
}
