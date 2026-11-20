package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

/**
 * An artwork as the frontend reads it ({@code Artwork} of its domain): catalog values as ids, photos in order.
 */
public record ArtworkDataResponse(
        Long id,
        Long storeId,
        String slug,
        String titleEs,
        String titleEn,
        String historyEs,
        String historyEn,
        String technique,
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal depthCm,
        BigDecimal weightG,
        Long shapeId,
        Long styleId,
        List<Long> colorIds,
        List<Long> motifIds,
        SaleMode saleMode,
        ArtworkStatus status,
        BigDecimal price,
        String currency,
        Integer stock,
        List<PhotoDataResponse> photos,
        Instant publishedAt,
        Instant createdAt) {
}
