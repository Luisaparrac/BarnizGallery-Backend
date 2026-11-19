package com.barnizgallery.backend.dto;

import java.util.List;

import com.barnizgallery.backend.enums.StoreStatus;

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
        StoreStatus status,
        List<ArtworkSummaryResponse> artworks) {
}
