package com.barnizgallery.backend.config;

import java.time.Duration;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * {@code app.storage.cloudinary.*} properties. Without the cloud name, key and secret, photo upload stays
 * disabled (photos can still be registered by URL).
 *
 * @param cloudName {@code CLOUDINARY_CLOUD_NAME}
 * @param apiKey    {@code CLOUDINARY_API_KEY}
 * @param apiSecret {@code CLOUDINARY_API_SECRET}; only used to sign requests, never sent
 * @param baseUrl   Cloudinary API base URL
 * @param folder    root folder of the uploaded images
 * @param timeout   connect and read timeout of each call
 */
@ConfigurationProperties(prefix = "app.storage.cloudinary")
public record CloudinaryProperties(String cloudName, String apiKey, String apiSecret, String baseUrl,
        String folder, Duration timeout) {

    /** True only when the cloud name, the key and the secret are all set. */
    public boolean isConfigured() {
        return notBlank(cloudName) && notBlank(apiKey) && notBlank(apiSecret);
    }

    private static boolean notBlank(String value) {
        return value != null && !value.isBlank();
    }
}
