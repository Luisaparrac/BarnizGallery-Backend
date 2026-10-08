package com.barnizgallery.backend.patterns.adapter;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * <b>Adapter pattern – default implementation of the Target.</b>
 * <p>
 * Used while {@code STORAGE_PROVIDER} is {@code none}. Uploads answer HTTP 503;
 * photos can still be registered by URL.
 */
@Component
@ConditionalOnProperty(name = "app.storage.provider", havingValue = "none", matchIfMissing = true)
public class DisabledStorageService implements StorageService {

    @Override
    public String upload(MultipartFile file, String folder) {
        throw new FeatureDisabledException("Photo storage is not configured; register photos by URL instead");
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
