package com.barnizgallery.backend.dto;

/**
 * A photo of an artwork.
 */
public record PhotoResponse(
        Long photoId,
        Long artworkId,
        String fileUrl,
        String angle) {
}
