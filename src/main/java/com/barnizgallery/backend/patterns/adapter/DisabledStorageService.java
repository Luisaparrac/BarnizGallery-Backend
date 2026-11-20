package com.barnizgallery.backend.patterns.adapter;

import org.springframework.web.multipart.MultipartFile;

import com.barnizgallery.backend.enums.ErrorCode;
import com.barnizgallery.backend.exception.FeatureDisabledException;

/**
 * <b>Adapter pattern – default implementation of the Target.</b>
 * <p>
 * Used while the Cloudinary credentials are empty (see {@code StorageConfig}). Uploads answer HTTP 503;
 * photos can still be registered by URL.
 */
public class DisabledStorageService implements StorageService {

    @Override
    public StoredImage upload(MultipartFile file, String folder) {
        throw new FeatureDisabledException(ErrorCode.STORAGE_DISABLED);
    }

    @Override
    public void delete(String storageKey) {
        throw new FeatureDisabledException(ErrorCode.STORAGE_DISABLED);
    }

    @Override
    public boolean isEnabled() {
        return false;
    }
}
