package com.barnizgallery.backend.dto;

import java.util.List;

/**
 * A store with its master and its artworks.
 */
public record StoreDetailResponse(
        Long storeId,
        MasterResponse master,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        List<ArtworkSummaryResponse> artworks) {
}
