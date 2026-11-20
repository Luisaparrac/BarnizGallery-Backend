package com.barnizgallery.backend.dto;

import java.time.Instant;
import java.util.List;

import com.barnizgallery.backend.enums.StoreStatus;
import com.fasterxml.jackson.annotation.JsonRawValue;

/**
 * A store as the frontend reads it ({@code Store} of its domain).
 */
public record StoreDataResponse(Long id, Long masterId, String slug, String nameEs, String nameEn,
        String descriptionEs, String descriptionEn, Long templateId, StoreStatus status, String logoUrl,
        String bannerUrl, @JsonRawValue String theme, boolean acceptsCustomOrders, List<StoreSectionResponse> sections,
        Instant publishedAt) {
}
