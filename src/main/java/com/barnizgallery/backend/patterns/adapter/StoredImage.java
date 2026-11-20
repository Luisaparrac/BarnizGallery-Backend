package com.barnizgallery.backend.patterns.adapter;

/**
 * An image saved by a {@link StorageService}.
 *
 * @param storageKey id of the file in the provider (for example the Cloudinary {@code public_id}); needed to
 *                   delete it later. Saved in {@code photos.storage_key}.
 * @param url        public HTTPS URL of the image
 * @param width      width in pixels, or null if the provider does not say
 * @param height     height in pixels, or null if the provider does not say
 */
public record StoredImage(String storageKey, String url, Integer width, Integer height) {
}
