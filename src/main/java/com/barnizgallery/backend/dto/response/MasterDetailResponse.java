package com.barnizgallery.backend.dto.response;

/**
 * A master with its room (room is null if it has none yet).
 */
public record MasterDetailResponse(
        Integer masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo,
        RoomSummaryResponse room) {
}
