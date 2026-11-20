package com.barnizgallery.backend.config;

import java.net.http.HttpClient;
import java.time.Clock;
import java.time.Duration;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import com.barnizgallery.backend.patterns.adapter.AiServiceClient;
import com.barnizgallery.backend.patterns.adapter.AiServiceGateway;
import com.barnizgallery.backend.patterns.adapter.DisabledAiServiceGateway;
import com.barnizgallery.backend.patterns.proxy.AiServiceProxy;

/**
 * Creates the single {@link AiServiceGateway}: the HTTP client behind an {@link AiServiceProxy} (cache and
 * cool-down after failures) when the URL and the key are set, otherwise the disabled gateway (the backend keeps
 * working with its fixed rules).
 * <p>
 * It uses the JDK {@link HttpClient}: unlike {@code HttpURLConnection}, it returns the JSON body of a 401 answer,
 * so the {@code unauthorized} code reaches the logs.
 */
@Configuration
public class AiServiceConfig {

    private static final Logger log = LoggerFactory.getLogger(AiServiceConfig.class);
    private static final Duration CACHE_TTL = Duration.ofMinutes(2);
    private static final Duration COOL_DOWN = Duration.ofSeconds(30);

    @Bean
    public AiServiceGateway aiServiceGateway(AiServiceProperties properties, Clock clock) {
        if (!properties.isConfigured()) {
            log.info("AI service disabled: AI_SERVICE_URL or AI_SERVICE_KEY is empty");
            return new DisabledAiServiceGateway();
        }
        log.info("AI service enabled");
        return new AiServiceProxy(new AiServiceClient(restClient(properties)), clock, CACHE_TTL, COOL_DOWN);
    }

    private static RestClient restClient(AiServiceProperties properties) {
        HttpClient httpClient = HttpClient.newBuilder().connectTimeout(properties.timeout()).build();
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(properties.timeout());
        return RestClient.builder()
                .baseUrl(properties.url())
                .defaultHeader(AiServiceClient.API_KEY_HEADER, properties.key())
                .requestFactory(requestFactory)
                .build();
    }
}
