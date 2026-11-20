package com.barnizgallery.backend.dto;

import com.barnizgallery.backend.enums.StoreStatus;

/**
 * A store with its master name and number of artworks.
 */
public record StoreSummaryResponse(
        Long storeId,
        String slug,
        Long masterId,
        String masterName,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String logoUrl,
        String bannerUrl,
        StoreStatus status,
        long artworkCount,
        StoreDataResponse store,
        MasterDataResponse master) {
}
