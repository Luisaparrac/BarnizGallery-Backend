package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.StoreEditorResponse;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.entity.Store;
import com.barnizgallery.backend.entity.StoreSection;

/**
 * Converts stores and their sections to the editor DTOs.
 */
public final class StoreEditorMapper {

    private StoreEditorMapper() {
    }

    public static StoreEditorResponse toResponse(Store store, List<StoreSection> sections) {
        return new StoreEditorResponse(store.getStoreId(), store.getSlug(), store.getStatus(),
                store.getTemplate().getCode(), store.getNameEs(), store.getNameEn(), store.getDescriptionEs(),
                store.getDescriptionEn(), store.getLogoUrl(), store.getBannerUrl(), store.getTheme(),
                store.isAcceptsCustomOrders(), store.getPublishedAt(),
                sections.stream().map(StoreEditorMapper::toResponse).toList());
    }

    public static StoreSectionResponse toResponse(StoreSection section) {
        return new StoreSectionResponse(section.getStoreSectionId(), section.getSectionType(), section.getTitleEs(),
                section.getTitleEn(), section.getContentEs(), section.getContentEn(), section.getImageUrl(),
                section.getConfig(), section.getSortOrder(), section.isVisible(), section.getStoreSectionId(),
                section.getSectionType(), section.isVisible());
    }
}
