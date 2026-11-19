package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

/**
 * Short view of an artwork, used in lists.
 */
public record ArtworkSummaryResponse(
        Long artworkId,
        Long storeId,
        String titleEs,
        String titleEn,
        String technique,
        List<String> colorTags,
        List<String> motifTags,
        ArtworkStatus status,
        SaleMode saleMode,
        BigDecimal price,
        String currency,
        Integer stock) {
}
