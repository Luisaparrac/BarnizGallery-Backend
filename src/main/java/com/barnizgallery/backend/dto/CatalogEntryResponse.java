package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * An entry of a simple catalog (color, shape, motif, style or avatar). {@code hex} only appears for colors and
 * {@code imageUrl} only for avatars.
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public record CatalogEntryResponse(
        String code,
        String nameEs,
        String nameEn,
        Integer sortOrder,
        boolean active,
        String hex,
        String imageUrl,
        Long id,
        boolean isActive) {
}
