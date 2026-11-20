package com.barnizgallery.backend.patterns.adapter;

import org.springframework.web.multipart.MultipartFile;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the system uses to store uploaded images (photos). Render's disk is ephemeral, so files go to
 * an external storage; an adapter per provider (Cloudinary today) translates these calls into its API.
 */
public interface StorageService {

    /**
     * Uploads an image and returns where it was saved.
     *
     * @param folder logical folder, for example {@code artworks/12}
     */
    StoredImage upload(MultipartFile file, String folder);

    /** Deletes a stored image by its {@link StoredImage#storageKey()}. */
    void delete(String storageKey);

    boolean isEnabled();
}
