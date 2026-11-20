package com.barnizgallery.backend.dto;

import java.math.BigDecimal;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * An artwork of the public gallery with its store, master, photos ({@code [{photo_id, file_url, alt_es, alt_en}]})
 * and catalog codes. {@code artwork} and {@code favoriteCount} are the shape the frontend reads
 * ({@code ArtworkSummary}).
 */
public record CatalogArtworkResponse(
        Long artworkId,
        String slug,
        String titleEs,
        String titleEn,
        String technique,
        SaleMode saleMode,
        BigDecimal price,
        String currency,
        Integer stock,
        ArtworkStatus status,
        Long storeId,
        String storeSlug,
        String storeNameEs,
        String storeNameEn,
        Long masterId,
        String masterName,
        @JsonRawValue String photos,
        @JsonRawValue String colorCodes,
        @JsonRawValue String motifCodes,
        ArtworkDataResponse artwork,
        long favoriteCount) {
}
