package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.StorePageResponse;
import com.barnizgallery.backend.dto.StoreSectionResponse;
import com.barnizgallery.backend.dto.StoreSummaryResponse;
import com.barnizgallery.backend.entity.Master;
import com.barnizgallery.backend.entity.Store;

/**
 * Converts between {@link Store} and its DTOs.
 */
public final class StoreMapper {

    private StoreMapper() {
    }

    public static StoreSummaryResponse toSummary(Store store, long artworkCount) {
        Master master = store.getMaster();
        return new StoreSummaryResponse(store.getStoreId(), store.getSlug(), master.getMasterId(), master.getName(),
                store.getNameEs(), store.getNameEn(), store.getDescriptionEs(), store.getDescriptionEn(),
                store.getLogoUrl(), store.getBannerUrl(), store.getStatus(), artworkCount);
    }

    public static StorePageResponse toPage(Store store, List<StoreSectionResponse> sections,
            List<ArtworkSummaryResponse> artworks) {
        return new StorePageResponse(store.getStoreId(), store.getSlug(), store.getNameEs(), store.getNameEn(),
                store.getDescriptionEs(), store.getDescriptionEn(), store.getLogoUrl(), store.getBannerUrl(),
                store.getTheme(), store.isAcceptsCustomOrders(), store.getPublishedAt(),
                MasterMapper.toResponse(store.getMaster()), sections, artworks);
    }
}
