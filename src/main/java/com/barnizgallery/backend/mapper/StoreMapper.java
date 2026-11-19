package com.barnizgallery.backend.mapper;

import java.util.List;

import com.barnizgallery.backend.dto.ArtworkSummaryResponse;
import com.barnizgallery.backend.dto.StoreDetailResponse;
import com.barnizgallery.backend.dto.StoreRequest;
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
        return new StoreSummaryResponse(store.getStoreId(), master.getMasterId(), master.getName(), store.getNameEs(),
                store.getNameEn(), store.getDescriptionEs(), store.getDescriptionEn(), artworkCount);
    }

    public static StoreDetailResponse toDetail(Store store, List<ArtworkSummaryResponse> artworks) {
        return new StoreDetailResponse(store.getStoreId(), MasterMapper.toResponse(store.getMaster()), store.getNameEs(),
                store.getNameEn(), store.getDescriptionEs(), store.getDescriptionEn(), artworks);
    }

    /** Copies the request fields into the entity. The master is set by the service. */
    public static void apply(StoreRequest request, Store store) {
        store.setNameEs(request.nameEs());
        store.setNameEn(request.nameEn());
        store.setDescriptionEs(request.descriptionEs());
        store.setDescriptionEn(request.descriptionEn());
    }
}
