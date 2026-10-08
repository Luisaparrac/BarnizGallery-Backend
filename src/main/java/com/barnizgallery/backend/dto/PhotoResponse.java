package com.barnizgallery.backend.dto;

/**
 * A photo of an artwork.
 */
public record PhotoResponse(
        Integer photoId,
        Integer artworkId,
        String fileUrl,
        String angle) {
}
