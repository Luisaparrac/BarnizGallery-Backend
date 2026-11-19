package com.barnizgallery.backend.dto;

/**
 * A room with its master name and number of artworks.
 */
public record RoomSummaryResponse(
        Long roomId,
        Long masterId,
        String masterName,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        long artworkCount) {
}
