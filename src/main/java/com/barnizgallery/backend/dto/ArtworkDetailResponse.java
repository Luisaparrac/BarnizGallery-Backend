package com.barnizgallery.backend.dto;

import java.math.BigDecimal;
import java.util.List;

import com.barnizgallery.backend.enums.ArtworkStatus;
import com.barnizgallery.backend.enums.SaleMode;

/**
 * Full artwork: data, photos and current auction (null if none).
 *
 * @param dimensionsLegacy free-text dimensions kept from v1 (null for new artworks)
 */
public record ArtworkDetailResponse(
        Long artworkId,
        Long storeId,
        String titleEs,
        String titleEn,
        String historyEs,
        String historyEn,
        String technique,
        String dimensionsLegacy,
        BigDecimal widthCm,
        BigDecimal heightCm,
        BigDecimal depthCm,
        BigDecimal weightG,
        List<String> colorCodes,
        List<String> motifCodes,
        String shapeCode,
        String styleCode,
        ArtworkStatus status,
        SaleMode saleMode,
        BigDecimal price,
        String currency,
        Integer stock,
        List<PhotoResponse> photos,
        AuctionResponse currentAuction,
        ArtworkDataResponse artwork,
        String storeSlug,
        String storeNameEs,
        String storeNameEn,
        Long masterId,
        String masterName,
        long favoriteCount,
        Long activeAuctionId,
        boolean isFavorite) {
}
