package com.barnizgallery.backend.dto;

/**
 * A master artisan.
 */
public record MasterResponse(
        Long masterId,
        String name,
        String biographyEs,
        String biographyEn,
        String workshop,
        String contactInfo) {
}
