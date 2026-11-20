package com.barnizgallery.backend.dto;

import java.time.Instant;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * Public page of a published store: its look, its visible sections in order and its exhibited or auctioned
 * artworks.
 */
public record StorePageResponse(
        Long storeId,
        String slug,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String logoUrl,
        String bannerUrl,
        @JsonRawValue String theme,
        boolean acceptsCustomOrders,
        Instant publishedAt,
        MasterResponse master,
        List<StoreSectionResponse> sections,
        List<ArtworkSummaryResponse> artworks) {
}
