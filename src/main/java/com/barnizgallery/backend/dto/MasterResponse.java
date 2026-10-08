package com.barnizgallery.backend.dto;

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
