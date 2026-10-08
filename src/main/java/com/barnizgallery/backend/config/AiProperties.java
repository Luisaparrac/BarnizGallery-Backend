package com.barnizgallery.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.ai.*} properties.
 *
 * @param provider AI provider name ({@code none} disables AI)
 * @param apiKey   API key of the provider
 */
@ConfigurationProperties(prefix = "app.ai")
public record AiProperties(String provider, String apiKey) {
}
