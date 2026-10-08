package com.barnizgallery.backend.config;

import java.util.List;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.cors.*} properties.
 *
 * @param allowedOrigins frontend origins allowed to call the REST API and the WebSocket
 */
@ConfigurationProperties(prefix = "app.cors")
public record CorsProperties(List<String> allowedOrigins) {
}
