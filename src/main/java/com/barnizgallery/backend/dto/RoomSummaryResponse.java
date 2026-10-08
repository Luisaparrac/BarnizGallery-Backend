package com.barnizgallery.backend.dto;

/**
 * A room with its master name and number of artworks.
 */
public record RoomSummaryResponse(
        Integer roomId,
        Integer masterId,
        String masterName,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String scene3dUrl,
        long artworkCount) {
}
