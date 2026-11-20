package com.barnizgallery.backend.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.Clock;
import java.time.Duration;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.barnizgallery.backend.exception.FeatureDisabledException;
import com.barnizgallery.backend.patterns.adapter.CloudinaryStorageService;
import com.barnizgallery.backend.patterns.adapter.StorageService;

class StorageConfigTest {

    private final StorageConfig config = new StorageConfig();

    @Test
    void missingCredentialsDisableUploads() {
        StorageService storage = config.storageService(properties("", ""), Clock.systemUTC());

        assertThat(storage.isEnabled()).isFalse();
        assertThatThrownBy(() -> storage.upload(null, "artworks/1")).isInstanceOf(FeatureDisabledException.class)
                .hasMessage("storage_disabled");
    }

    @Test
    void completeCredentialsSelectCloudinary() {
        StorageService storage = config.storageService(
                properties(UUID.randomUUID().toString(), UUID.randomUUID().toString()), Clock.systemUTC());

        assertThat(storage).isInstanceOf(CloudinaryStorageService.class);
        assertThat(storage.isEnabled()).isTrue();
    }

    private static CloudinaryProperties properties(String key, String secret) {
        return new CloudinaryProperties("demo-cloud", key, secret, "https://cloudinary.test", "museo",
                Duration.ofSeconds(30));
    }
}
