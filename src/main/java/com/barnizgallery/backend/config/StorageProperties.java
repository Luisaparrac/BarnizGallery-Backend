package com.barnizgallery.backend.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.storage.*} properties.
 *
 * @param provider photo storage provider ({@code none} disables uploads)
 */
@ConfigurationProperties(prefix = "app.storage")
public record StorageProperties(String provider) {
}
