package com.barnizgallery.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code ai.service.*} properties of the separate MUSEO AI service.
 *
 * @param url     base URL of the service, without a trailing slash ({@code AI_SERVICE_URL}); empty disables it
 * @param key     shared key sent in the {@code X-API-Key} header ({@code AI_SERVICE_KEY}); empty disables it
 * @param timeout connect and read timeout of the price and recommendation calls
 * @param designTimeout read timeout of the image calls ({@code POST /v1/designs})
 */
@ConfigurationProperties(prefix = "ai.service")
public record AiServiceProperties(String url, String key, Duration timeout, Duration designTimeout) {

    /** True only when both the URL and the key are set. */
    public boolean isConfigured() {
        return url != null && !url.isBlank() && key != null && !key.isBlank();
    }
}
