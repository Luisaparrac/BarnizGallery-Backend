package com.barnizgallery.backend.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.patterns.adapter.AiServiceClient;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.adapter.DisabledAiServiceGateway;

/**
 * Creates the single {@link AiServiceGateway}: the HTTP client when the URL and the key are set, otherwise the
 * disabled gateway (the backend keeps working with its fixed rules).
 */
@Configuration
public class AiServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(AiServiceConfig.class);

    @Bean
    public AiServiceGateway aiServiceGateway(AiServiceProperties properties) {
        if (!properties.isConfigured()) {
            log.info("AI service disabled: AI_SERVICE_URL or AI_SERVICE_KEY is empty");
            return new DisabledAiServiceGateway();
        }
        log.info("AI service enabled");
        return new AiServiceClient(restClient(properties));
    }

    private static RestClient restClient(AiServiceProperties properties) {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(properties.timeout());
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.url())
                .defaultHeader(AiServiceClient.API_KEY_HEADER, properties.key())
                .requestFactory(requestFactory)
                .build();
    }
}
