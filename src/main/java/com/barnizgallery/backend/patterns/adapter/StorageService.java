package com.barnizgallery.backend.patterns.adapter;

import org.springframework.web.multipart.MultipartFile;

/**
 * <b>Adapter pattern – Target.</b>
 * <p>
 * The interface the system uses to store uploaded files (photos). Render's disk is
 * ephemeral, so files must go to an external storage (S3, Cloudinary, Supabase...).
 * The provider is not chosen yet; an adapter per provider will implement this interface.
 */
public interface StorageService {

    /**
     * Uploads a file and returns its public URL.
     *
     * @param folder logical folder, for example {@code artworks/12}
     */
    String upload(MultipartFile file, String folder);

    boolean isEnabled();
}
