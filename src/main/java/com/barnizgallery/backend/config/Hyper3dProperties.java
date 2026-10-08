package com.barnizgallery.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.hyper3d.*} properties.
 *
 * @param apiKey  Hyper3D Rodin API key (empty disables 3D generation)
 * @param baseUrl Hyper3D API base URL
 */
@ConfigurationProperties(prefix = "app.hyper3d")
public record Hyper3dProperties(String apiKey, String baseUrl) {
}
