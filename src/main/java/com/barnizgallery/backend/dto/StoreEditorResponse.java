package com.barnizgallery.backend.dto;

import java.time.Instant;
import java.util.List;

import com.barnizgallery.backend.enums.StoreStatus;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * The artisan's own store as the editor shows it: every section, visible or hidden.
 */
public record StoreEditorResponse(
        Long storeId,
        String slug,
        StoreStatus status,
        String templateCode,
        String nameEs,
        String nameEn,
        String descriptionEs,
        String descriptionEn,
        String logoUrl,
        String bannerUrl,
        @JsonRawValue String theme,
        boolean acceptsCustomOrders,
        Instant publishedAt,
        List<StoreSectionResponse> sections) {
}
