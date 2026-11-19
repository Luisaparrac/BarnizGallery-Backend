package com.barnizgallery.backend.dto;

import java.util.List;

/**
 * A room with its master and its artworks.
 */
public record RoomDetailResponse(
        Long roomId,
        MasterResponse master,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        List<ArtworkSummaryResponse> artworks) {
}
