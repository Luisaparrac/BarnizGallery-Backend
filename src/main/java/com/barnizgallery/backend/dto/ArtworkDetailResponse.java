package com.barnizgallery.backend.dto;

import java.util.List;

import com.barnizgallery.backend.enums.ArtworkStatus;

/**
 * Full artwork: data, photos, 3D model and current auction (null if none).
 */
public record ArtworkDetailResponse(
        Integer artworkId,
        Integer roomId,
        String titleEs,
        String titleEn,
        String historyEs,
        String historyEn,
        String technique,
        String dimensions,
        List<String> colorTags,
        List<String> motifTags,
        ArtworkStatus status,
        List<PhotoResponse> photos,
        ThreeDModelResponse model,
        AuctionResponse currentAuction) {
}
