package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A store template. {@code defaultLayout} and {@code defaultTheme} are the JSON a new store starts with.
 */
public record StoreTemplateResponse(
        String code,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String previewUrl,
        @JsonRawValue String defaultLayout,
        @JsonRawValue String defaultTheme,
        boolean active,
        Long id,
        boolean isActive) {
}
