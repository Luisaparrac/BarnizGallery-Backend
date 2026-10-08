package com.barnizgallery.backend.dto.response;

/**
 * A master artisan.
 */
public record MasterResponse(
        Integer masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo) {
}
