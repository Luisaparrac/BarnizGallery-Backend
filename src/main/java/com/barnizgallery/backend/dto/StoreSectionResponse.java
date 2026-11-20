package com.barnizgallery.backend.dto;

import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A section of a store page (for example a hero banner, the artisan's story or a gallery block).
 */
public record StoreSectionResponse(
        Long sectionId,
        String sectionType,
        String titleEs,
        String titleEn,
        String contentEs,
        String contentEn,
        String imageUrl,
        @JsonRawValue String config,
        Integer sortOrder,
        boolean visible) {
}
