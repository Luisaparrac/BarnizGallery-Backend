package com.barnizgallery.backend.dto;

/**
 * A photo of an artwork.
 *
 * @param primary true for the main photo of the artwork (at most one)
 */
public record PhotoResponse(
        Long photoId,
        Long artworkId,
        String fileUrl,
        String angle,
        String altEs,
        String altEn,
        Integer sortOrder,
        boolean primary) {
}
