package com.barnizgallery.backend.dto.response;

/**
 * A photo of an artwork.
 */
public record PhotoResponse(
        Integer photoId,
        Integer artworkId,
        String fileUrl,
        String angle) {
}
