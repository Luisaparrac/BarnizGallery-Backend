package com.barnizgallery.backend.dto;

/**
 * A photo as the frontend reads it ({@code Photo} of its domain).
 */
public record PhotoDataResponse(Long id, String url, String altEs, String altEn, String angle, Integer sortOrder,
        boolean isPrimary) {
}
