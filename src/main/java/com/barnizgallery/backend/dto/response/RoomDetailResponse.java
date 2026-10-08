package com.barnizgallery.backend.dto.response;

import java.util.List;

/**
 * A room with its master and its artworks.
 */
public record RoomDetailResponse(
        Integer roomId,
        MasterResponse master,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String scene3dUrl,
        List<ArtworkSummaryResponse> artworks) {
}
