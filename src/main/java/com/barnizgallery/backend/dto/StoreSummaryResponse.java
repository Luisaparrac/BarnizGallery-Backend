package com.barnizgallery.backend.dto;

/**
 * A store with its master name and number of artworks.
 */
public record StoreSummaryResponse(
        Long storeId,
        Long masterId,
        String masterName,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        long artworkCount) {
}
