package com.barnizgallery.backend.dto.response;

import java.util.List;

import com.barnizgallery.backend.model.enums.ArtworkStatus;

/**
 * Main data of an artwork, used in lists.
 */
public record ArtworkSummaryResponse(
        Integer artworkId,
        Integer roomId,
        String titleEs,
        String titleEn,
        String technique,
        String dimensions,
        List<String> colorTags,
        List<String> motifTags,
        ArtworkStatus status) {
}
